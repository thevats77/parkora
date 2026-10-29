/*
 * DATA MODELS
 * -----------
 * These TypeScript types are the same as the Kotlin data classes in the
 * Android app (android/app/src/main/java/com/example/parkora/data/Models.kt),
 * field for field. Keeping them identical is what will later let the website
 * and the Android app share one backend (see docs/SHARED_BACKEND.md).
 */

export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  location: string;              // e.g. "Indirapuram, Ghaziabad, India"
  createdAt: number;             // milliseconds since 1970 (same as Kotlin Long)
  profileImagePath: string | null; // Google photo URL or an uploaded photo (data URL)
  role: "user" | "admin";
  isBlocked: boolean;              // set by the admin
}

/** A time range already taken on a spot (stored in the spot – used for the double-booking check). */
export interface BookedSlot {
  bookingId: string;
  startMillis: number;
  endMillis: number;
}

export interface ParkingSpot {
  id: string;
  ownerId: string;
  ownerName: string;
  // Step 1 – Location / Address
  parkingNumber: string;
  societyName: string;
  address: string;
  city: string;
  // Step 2 – Parking Size
  parkingType: string;           // "Open" | "Covered" | "Basement"
  lengthFeet: number;
  widthFeet: number;
  vehicleTypes: string[];        // e.g. ["SUV", "Sedan"]
  // Step 3 – Upload Documents
  imagePaths: string[];          // "demo_parking_1" or a compressed photo ("data:image/jpeg;base64,...")
  documentNames: string[];       // only the file names are recorded
  documentPaths: string[];
  // Step 4 – Pricing
  pricePerHour: number;
  pricePerDay: number;
  availableForRent: boolean;
  availableForSale: boolean;
  isAvailable: boolean;
  isVerified: boolean;
  rating: number;                // 0–5 (0 = new)
  createdAt: number;
  bookedSlots: BookedSlot[];     // active bookings (pending/approved)
}

export type BookingStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED" | "COMPLETED" | "EXPIRED";

export const statusLabel: Record<BookingStatus, string> = {
  PENDING: "Pending",
  APPROVED: "Approved",
  REJECTED: "Rejected",
  CANCELLED: "Cancelled",
  COMPLETED: "Completed",
  EXPIRED: "Expired",
};

export interface Booking {
  id: string;
  spotId: string;
  parkingNumber: string;
  spotImage: string;
  userId: string;
  userName: string;
  ownerId: string;
  checkInMillis: number;
  checkOutMillis: number;
  billedUnits: number;
  billedUnitName: string;        // "Hour(s)" or "Day(s)"
  unitPrice: number;
  totalPrice: number;
  status: BookingStatus;
  createdAt: number;
}

export interface AppNotification {
  id: string;
  userId: string;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: number;
}
