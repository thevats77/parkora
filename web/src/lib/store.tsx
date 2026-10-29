"use client";

import { onAuthStateChanged, signInWithPopup, signOut, type User as FirebaseUser } from "firebase/auth";
import {
  arrayRemove, collection, doc, getDoc, onSnapshot, query, runTransaction, setDoc, updateDoc, where, writeBatch,
  type DocumentData, type Unsubscribe,
} from "firebase/firestore";
import { createContext, useContext, useEffect, useRef, useState } from "react";
import { calculatePrice } from "./bookingCalculator";
import { ADMIN_EMAIL, auth, db, googleProvider, isFirebaseConfigured } from "./firebase";
import { mujDemoSpots } from "./mujDemoSpots";
import type { AppNotification, BookedSlot, Booking, BookingStatus, ParkingSpot, User } from "./types";

/*
 * PARKORA STORE  (the website's data layer)
 * -----------------------------------------
 * The web version of ParkoraRepository.kt. Same function names, same rules, and
 * the SAME live database: Firebase Authentication (Google sign-in) + Cloud Firestore.
 *
 * HOW IT WORKS
 *   - onAuthStateChanged tells us who is signed in.
 *   - onSnapshot "listeners" receive every change in Firestore immediately
 *     (made on the website OR in the Android app) and put it into React state,
 *     so pages re-render by themselves.
 *   - Functions that change data are async and return an error message, or null when they worked.
 *   - The security rules (android/firebase/firestore.rules) are checked by Google's servers.
 *
 * HOW PAGES USE IT:   const { currentUser, createBooking } = useParkora();
 */

type Result = string | null;

export interface ParkoraData {
  users: User[];               // only filled for the admin
  parkingSpots: ParkingSpot[];
  bookings: Booking[];         // mine + for my spots (admin: all)
  notifications: AppNotification[];
}

interface ParkoraStore {
  ready: boolean;              // false until we know who is signed in and spots have loaded
  data: ParkoraData;
  currentUser: User | null;
  isAdmin: boolean;
  // auth & profile
  signInWithGoogle(): Promise<Result>;
  logout(): Promise<void>;
  updateProfile(name: string, phone: string, location: string): Promise<Result>;
  updateProfilePhoto(dataUrl: string): Promise<Result>;
  // parking spots
  latestSpots(): ParkingSpot[];
  getSpot(id: string): ParkingSpot | undefined;
  mySpots(): ParkingSpot[];
  addParkingSpot(spot: Omit<ParkingSpot, "id" | "ownerId" | "ownerName" | "createdAt" | "bookedSlots">): Promise<Result>;
  setSpotAvailability(id: string, available: boolean): Promise<Result>;
  deleteSpot(id: string): Promise<Result>;
  // bookings
  myBookings(): Booking[];
  bookingRequestsForMe(): Booking[];
  createBooking(spot: ParkingSpot, checkIn: number, checkOut: number): Promise<Result>;
  canCancel(booking: Booking): boolean;
  cancelBooking(id: string): Promise<Result>;
  respondToBooking(id: string, approve: boolean): Promise<Result>;
  // notifications
  myNotifications(): AppNotification[];
  unreadCount(): number;
  markMyNotificationsRead(): void;
  // admin portal
  adminSetSpotVerified(id: string, verified: boolean): Promise<Result>;
  adminDeleteSpot(id: string): Promise<Result>;
  adminCancelBooking(id: string): Promise<Result>;
  adminSetUserBlocked(id: string, blocked: boolean): Promise<Result>;
  adminAddMujDemoSpots(): Promise<Result>;
}

const ParkoraContext = createContext<ParkoraStore | null>(null);

export const isOngoing = (b: Booking) => b.status === "PENDING" || b.status === "APPROVED";

// Firestore collection names (same as the Android app)
const USERS = "users", SPOTS = "parking_spots", BOOKINGS = "bookings", NOTIFICATIONS = "notifications";

class BookingClashError extends Error {}

/** Turns a Firestore document into one of our types (adds the document id). */
const fromDoc = <T,>(d: { id: string; data(): DocumentData | undefined }) => ({ ...(d.data() ?? {}), id: d.id }) as T;

const spotFromDoc = (d: { id: string; data(): DocumentData | undefined }): ParkingSpot => {
  const s = fromDoc<ParkingSpot>(d);
  return { ...s, bookedSlots: s.bookedSlots ?? [], imagePaths: s.imagePaths ?? [], vehicleTypes: s.vehicleTypes ?? [] };
};

/** Runs a database action with a time limit and turns errors into friendly messages. */
async function attempt(action: () => Promise<unknown>): Promise<Result> {
  if (!isFirebaseConfigured) return "Firebase is not set up yet (see web/src/lib/firebase.ts).";
  try {
    await Promise.race([
      action(),
      new Promise((_, reject) => setTimeout(() => reject(new Error("timeout")), 15_000)),
    ]);
    return null;
  } catch (e) {
    const err = e as { code?: string; message?: string };
    if (e instanceof BookingClashError) return "This spot is already booked for part of that time. Please choose another time.";
    if (err.message === "timeout") return "Could not reach the server. Please check your internet connection.";
    if (err.code === "permission-denied") return "You are not allowed to do that.";
    if (err.code === "unavailable") return "No internet connection. Please try again.";
    return err.message ?? "Something went wrong. Please try again.";
  }
}

function newNotification(userId: string, title: string, message: string): [string, AppNotification] {
  const ref = doc(collection(db, NOTIFICATIONS));
  return [ref.id, { id: ref.id, userId, title, message, isRead: false, createdAt: Date.now() }];
}

const slotOf = (b: Booking): BookedSlot => ({ bookingId: b.id, startMillis: b.checkInMillis, endMillis: b.checkOutMillis });

export function ParkoraProvider({ children }: { children: React.ReactNode }) {
  const [authChecked, setAuthChecked] = useState(!isFirebaseConfigured);
  const [spotsLoaded, setSpotsLoaded] = useState(!isFirebaseConfigured);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [parkingSpots, setParkingSpots] = useState<ParkingSpot[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const userListeners = useRef<Unsubscribe[]>([]);

  // ---------- Parking spots are public: listen even before sign-in ----------
  useEffect(() => {
    if (!isFirebaseConfigured) return;
    return onSnapshot(collection(db, SPOTS), (snap) => {
      setParkingSpots(snap.docs.map(spotFromDoc));
      setSpotsLoaded(true);
    }, () => setSpotsLoaded(true));
  }, []);

  // ---------- Who is signed in? ----------
  useEffect(() => {
    if (!isFirebaseConfigured) return;
    return onAuthStateChanged(auth, async (firebaseUser) => {
      userListeners.current.forEach((stop) => stop());
      userListeners.current = [];
      setBookings([]); setNotifications([]); setUsers([]);
      if (!firebaseUser) {
        setCurrentUser(null);
        setAuthChecked(true);
        return;
      }
      try {
        const user = await loadOrCreateUser(firebaseUser);
        setCurrentUser(user);
        startUserListeners(user);
      } catch {
        await signOut(auth);
        setCurrentUser(null);
      }
      setAuthChecked(true);
    });
  }, []);

  /** First sign-in creates users/{uid} from the Google profile. */
  async function loadOrCreateUser(fu: FirebaseUser): Promise<User> {
    const ref = doc(db, USERS, fu.uid);
    const snap = await getDoc(ref);
    if (snap.exists()) return fromDoc<User>(snap);
    const email = fu.email ?? "";
    const user: User = {
      id: fu.uid, name: fu.displayName ?? email.split("@")[0], email, phone: "",
      location: "Dehmi Kalan, Jaipur", createdAt: Date.now(), profileImagePath: fu.photoURL,
      role: email.toLowerCase() === ADMIN_EMAIL ? "admin" : "user", isBlocked: false,
    };
    await setDoc(ref, user);
    return user;
  }

  function startUserListeners(user: User) {
    const add = (u: Unsubscribe) => userListeners.current.push(u);
    add(onSnapshot(doc(db, USERS, user.id), (d) => { if (d.exists()) setCurrentUser(fromDoc<User>(d)); }));

    if (user.role === "admin") {
      add(onSnapshot(collection(db, BOOKINGS), (s) => setBookings(s.docs.map((d) => fromDoc<Booking>(d)))));
      add(onSnapshot(collection(db, USERS), (s) =>
        setUsers(s.docs.map((d) => fromDoc<User>(d)).sort((a, b) => b.createdAt - a.createdAt))));
    } else {
      // bookings I made + bookings for my spots, merged
      let mine: Booking[] = [], forMySpots: Booking[] = [];
      const merge = () => setBookings([...new Map([...mine, ...forMySpots].map((b) => [b.id, b])).values()]);
      add(onSnapshot(query(collection(db, BOOKINGS), where("userId", "==", user.id)), (s) => { mine = s.docs.map((d) => fromDoc<Booking>(d)); merge(); }));
      add(onSnapshot(query(collection(db, BOOKINGS), where("ownerId", "==", user.id)), (s) => { forMySpots = s.docs.map((d) => fromDoc<Booking>(d)); merge(); }));
    }
    add(onSnapshot(query(collection(db, NOTIFICATIONS), where("userId", "==", user.id)), (s) =>
      setNotifications(s.docs.map((d) => fromDoc<AppNotification>(d)))));
  }

  // ---------- Approved -> Completed, Pending -> Expired after check-out ----------
  useEffect(() => {
    if (!currentUser) return;
    const now = Date.now();
    const finished = bookings.filter((b) => b.checkOutMillis < now && isOngoing(b)
      && (b.userId === currentUser.id || b.ownerId === currentUser.id || currentUser.role === "admin"));
    if (!finished.length) return;
    const batch = writeBatch(db);
    finished.forEach((b) => batch.update(doc(db, BOOKINGS, b.id), { status: b.status === "APPROVED" ? "COMPLETED" : "EXPIRED" }));
    batch.commit().catch(() => {});
  }, [bookings, currentUser]);

  const blocked = (): Result => (currentUser?.isBlocked ? "Your account has been blocked by the admin." : null);

  // ================= Auth & profile =================
  async function signInWithGoogle(): Promise<Result> {
    if (!isFirebaseConfigured) return "Firebase is not set up yet (see web/src/lib/firebase.ts).";
    try {
      await signInWithPopup(auth, googleProvider);
      return null; // onAuthStateChanged loads the profile
    } catch (e) {
      const code = (e as { code?: string }).code;
      if (code === "auth/popup-closed-by-user" || code === "auth/cancelled-popup-request") return "Sign-in was cancelled.";
      if (code === "auth/popup-blocked") return "Your browser blocked the Google pop-up. Please allow pop-ups for this site.";
      if (code === "auth/unauthorized-domain") return "This website address is not yet allowed in Firebase (Authorized domains).";
      return `Google sign-in failed (${code ?? "unknown error"}).`;
    }
  }

  const logout = async () => { await signOut(auth); };

  const updateProfile = (name: string, phone: string, location: string) => attempt(() =>
    updateDoc(doc(db, USERS, currentUser!.id), { name: name.trim(), phone: phone.trim(), location: location.trim() }));

  const updateProfilePhoto = (dataUrl: string) =>
    attempt(() => updateDoc(doc(db, USERS, currentUser!.id), { profileImagePath: dataUrl }));

  // ================= Parking spots =================
  const latestSpots = () => parkingSpots.filter((s) => s.isAvailable).sort((a, b) => b.createdAt - a.createdAt);
  const getSpot = (id: string) => parkingSpots.find((s) => s.id === id);
  const mySpots = () => parkingSpots.filter((s) => s.ownerId === currentUser?.id).sort((a, b) => b.createdAt - a.createdAt);

  async function addParkingSpot(spot: Omit<ParkingSpot, "id" | "ownerId" | "ownerName" | "createdAt" | "bookedSlots">): Promise<Result> {
    if (!currentUser) return "Please sign in first";
    const b = blocked(); if (b) return b;
    const ref = doc(collection(db, SPOTS));
    const full: ParkingSpot = { ...spot, id: ref.id, ownerId: currentUser.id, ownerName: currentUser.name,
      isVerified: false, createdAt: Date.now(), bookedSlots: [] };
    return attempt(() => setDoc(ref, full));
  }

  const setSpotAvailability = (id: string, available: boolean) =>
    attempt(() => updateDoc(doc(db, SPOTS, id), { isAvailable: available }));

  async function deleteSpot(id: string): Promise<Result> {
    const now = Date.now();
    const active = getSpot(id)?.bookedSlots.some((s) => s.endMillis > now) || bookings.some((b) => b.spotId === id && isOngoing(b));
    if (active) return "This spot has active bookings. Pause it instead, or wait until they finish.";
    return attempt(() => writeBatch(db).delete(doc(db, SPOTS, id)).commit());
  }

  // ================= Bookings =================
  const myBookings = () => bookings.filter((b) => b.userId === currentUser?.id).sort((a, b) => a.checkInMillis - b.checkInMillis);
  const bookingRequestsForMe = () => bookings.filter((b) => b.ownerId === currentUser?.id).sort((a, b) => b.createdAt - a.createdAt);

  /**
   * Creates a PENDING booking. The double-booking check and the save happen in ONE
   * Firestore transaction, so two people cannot book the same time at the same moment.
   */
  async function createBooking(spot: ParkingSpot, checkIn: number, checkOut: number): Promise<Result> {
    const user = currentUser;
    if (!user) return "Please sign in first";
    const b = blocked(); if (b) return b;
    if (spot.ownerId === user.id) return "You cannot book your own parking spot";
    if (!spot.isAvailable || !spot.availableForRent) return "This spot is not available for booking right now";
    if (checkIn < Date.now() - 60_000) return "Check-in time cannot be in the past";
    if (checkOut <= checkIn) return "Check-out must be after check-in";
    const summary = calculatePrice(checkIn, checkOut, spot.pricePerHour, spot.pricePerDay);
    if (!summary) return "Please choose a valid check-in and check-out time";
    if (summary.totalHours > 30 * 24) return "A booking can be at most 30 days long";

    const spotRef = doc(db, SPOTS, spot.id);
    const bookingRef = doc(collection(db, BOOKINGS));
    const booking: Booking = {
      id: bookingRef.id, spotId: spot.id, parkingNumber: spot.parkingNumber, spotImage: spot.imagePaths[0] ?? "demo_parking_1",
      userId: user.id, userName: user.name, ownerId: spot.ownerId, checkInMillis: checkIn, checkOutMillis: checkOut,
      billedUnits: summary.billedUnits, billedUnitName: summary.unitName, unitPrice: summary.unitPrice,
      totalPrice: summary.totalPrice, status: "PENDING", createdAt: Date.now(),
    };
    const [nid, notification] = newNotification(spot.ownerId, "New booking request", `${user.name} wants to book your spot ${spot.parkingNumber}.`);

    return attempt(() => runTransaction(db, async (tx) => {
      const fresh = spotFromDoc(await tx.get(spotRef));
      if (fresh.bookedSlots.some((s) => checkIn < s.endMillis && checkOut > s.startMillis)) throw new BookingClashError();
      tx.update(spotRef, { bookedSlots: [...fresh.bookedSlots, slotOf(booking)] });
      tx.set(bookingRef, booking);
      tx.set(doc(db, NOTIFICATIONS, nid), notification);
    }));
  }

  const canCancel = (b: Booking) => isOngoing(b) && b.checkInMillis > Date.now();

  async function cancelBooking(id: string): Promise<Result> {
    const b = bookings.find((x) => x.id === id);
    if (!b) return "Booking not found";
    if (!canCancel(b)) return "This booking can no longer be cancelled";
    const [nid, n] = newNotification(b.ownerId, "Booking cancelled", `${b.userName} cancelled the booking for ${b.parkingNumber}.`);
    return attempt(() => writeBatch(db)
      .update(doc(db, BOOKINGS, id), { status: "CANCELLED" })
      .update(doc(db, SPOTS, b.spotId), { bookedSlots: arrayRemove(slotOf(b)) })
      .set(doc(db, NOTIFICATIONS, nid), n)
      .commit());
  }

  async function respondToBooking(id: string, approve: boolean): Promise<Result> {
    const b = bookings.find((x) => x.id === id);
    if (!b || b.status !== "PENDING") return "This request has already been answered";
    const status: BookingStatus = approve ? "APPROVED" : "REJECTED";
    const word = approve ? "approved" : "rejected";
    const [nid, n] = newNotification(b.userId, `Booking ${word}`, `Your booking for ${b.parkingNumber} has been ${word} by the owner.`);
    const batch = writeBatch(db).update(doc(db, BOOKINGS, id), { status }).set(doc(db, NOTIFICATIONS, nid), n);
    if (!approve) batch.update(doc(db, SPOTS, b.spotId), { bookedSlots: arrayRemove(slotOf(b)) });
    return attempt(() => batch.commit());
  }

  // ================= Notifications =================
  const myNotifications = () => [...notifications].sort((a, b) => b.createdAt - a.createdAt);
  const unreadCount = () => notifications.filter((n) => !n.isRead).length;

  function markMyNotificationsRead() {
    const unread = notifications.filter((n) => !n.isRead);
    if (!unread.length) return;
    const batch = writeBatch(db);
    unread.forEach((n) => batch.update(doc(db, NOTIFICATIONS, n.id), { isRead: true }));
    batch.commit().catch(() => {});
  }

  // ================= Admin Portal (allowed only for the admin by the security rules) =================
  const adminSetSpotVerified = (id: string, verified: boolean) =>
    attempt(() => updateDoc(doc(db, SPOTS, id), { isVerified: verified }));

  async function adminDeleteSpot(id: string): Promise<Result> {
    const spot = getSpot(id);
    if (!spot) return "Spot not found";
    const batch = writeBatch(db).delete(doc(db, SPOTS, id));
    bookings.filter((b) => b.spotId === id && isOngoing(b)).forEach((b) => {
      batch.update(doc(db, BOOKINGS, b.id), { status: "CANCELLED" });
      const [nid, n] = newNotification(b.userId, "Booking cancelled",
        `The listing ${spot.parkingNumber} was removed by the Parkora admin, so your booking was cancelled.`);
      batch.set(doc(db, NOTIFICATIONS, nid), n);
    });
    return attempt(() => batch.commit());
  }

  async function adminCancelBooking(id: string): Promise<Result> {
    const b = bookings.find((x) => x.id === id);
    if (!b) return "Booking not found";
    const batch = writeBatch(db)
      .update(doc(db, BOOKINGS, id), { status: "CANCELLED" })
      .update(doc(db, SPOTS, b.spotId), { bookedSlots: arrayRemove(slotOf(b)) });
    [...new Set([b.userId, b.ownerId])].forEach((uid) => {
      const [nid, n] = newNotification(uid, "Booking cancelled by admin",
        `The booking for ${b.parkingNumber} (${b.userName}) was cancelled by the Parkora admin.`);
      batch.set(doc(db, NOTIFICATIONS, nid), n);
    });
    return attempt(() => batch.commit());
  }

  const adminSetUserBlocked = (id: string, isBlocked: boolean) =>
    attempt(() => updateDoc(doc(db, USERS, id), { isBlocked }));

  async function adminAddMujDemoSpots(): Promise<Result> {
    if (!currentUser) return "Please sign in first";
    const batch = writeBatch(db);
    // merge = keep existing bookedSlots instead of wiping them
    mujDemoSpots(currentUser.id).forEach((s) => batch.set(doc(db, SPOTS, s.id), s, { merge: true }));
    return attempt(() => batch.commit());
  }

  const store: ParkoraStore = {
    ready: authChecked && spotsLoaded,
    data: { users, parkingSpots, bookings, notifications },
    currentUser, isAdmin: currentUser?.role === "admin",
    signInWithGoogle, logout, updateProfile, updateProfilePhoto,
    latestSpots, getSpot, mySpots, addParkingSpot, setSpotAvailability, deleteSpot,
    myBookings, bookingRequestsForMe, createBooking, canCancel, cancelBooking, respondToBooking,
    myNotifications, unreadCount, markMyNotificationsRead,
    adminSetSpotVerified, adminDeleteSpot, adminCancelBooking, adminSetUserBlocked, adminAddMujDemoSpots,
  };

  return <ParkoraContext.Provider value={store}>{children}</ParkoraContext.Provider>;
}

/** Use this in any component: const { currentUser } = useParkora(); */
export function useParkora(): ParkoraStore {
  const store = useContext(ParkoraContext);
  if (!store) throw new Error("useParkora must be used inside <ParkoraProvider>");
  return store;
}
