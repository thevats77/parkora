# PARKORA – Project Guide, Walkthrough & Viva Preparation (Version 2 – Live)

Smart Peer-to-Peer Parking & Booking Application — Android App Development Lab Project
Vatsal Sharma (Reg. No. 2427030235)

Version 2 turns the prototype into a **live, online platform**: everyone signs in with **Google**,
all data is stored in **Firebase Cloud Firestore**, the Android app and the website
(**https://parkora-tan.vercel.app**) share the **same live database**, and one admin account
has an **Admin Portal**. Four demo parking spots are listed around **Manipal University Jaipur (MUJ)**.

---

## 1. What the app is built with (be ready to say this)

| Item | What we used |
|---|---|
| Language | **Kotlin** |
| UI toolkit | **Jetpack Compose** (no XML layouts, no Fragments) |
| Screens | One **Activity** (`MainActivity`) + many `@Composable` screen functions |
| Navigation | **Navigation Compose** (`NavHost`, routes) |
| Design | **Material 3**, Work Sans font, yellow & white colours from the synopsis screenshots |
| Login | **Firebase Authentication** with **Google Sign-In** (Android Credential Manager) |
| Database | **Cloud Firestore** (online, real-time) – collections `users`, `parking_spots`, `bookings`, `notifications` |
| Security | **Firestore Security Rules** (`firebase/firestore.rules`) checked on Google's servers |
| Real-time updates | Firestore **snapshot listeners** → Compose state lists → screens redraw by themselves |
| Async code | **Kotlin coroutines** (`suspend` functions, `scope.launch { }`, `task.await()`) |
| Images | **Coil** (`AsyncImage`) – built-in demo pictures, uploaded photos, Google profile pictures |
| Website | Next.js + TypeScript + Tailwind (folder `../web`), same Firebase project |
| Build | Gradle (Kotlin DSL), AGP 9.4.1, compileSdk/targetSdk 37, minSdk 26 |

**Firebase project:** `parkora-muj-app` (free Spark plan) · **Admin account:** `vatsalsharma849@gmail.com`

**Honest limits:** no online payment (pay the owner directly), no maps/GPS (location is typed),
notifications are **in-app** (not push), photos are compressed and stored **inside Firestore documents**
because Firebase Storage needs a paid plan, for documents only the **file name** is recorded.
The app is installed from the APK (not on the Play Store).

---

## 2. Folder structure

```
android/                               ← open THIS folder in Android Studio
├── firebase.json, .firebaserc         ← Firebase CLI settings (project parkora-muj-app)
├── firebase/
│   ├── firestore.rules                ← SECURITY RULES (who may read/write what) – deployed to Google
│   └── firestore.indexes.json
├── docs/PARKORA_GUIDE.md              ← this guide
└── app/src/main/
    ├── AndroidManifest.xml            ← INTERNET permission + MainActivity
    ├── java/com/example/parkora/
    │   ├── MainActivity.kt            ← entry point (the only Activity)
    │   ├── data/                      ← data models + all database code
    │   │   ├── Models.kt              ← User, ParkingSpot, BookedSlot, Booking, AppNotification
    │   │   ├── FirebaseConfig.kt      ← project ids + ADMIN_EMAIL
    │   │   ├── FirestoreMapper.kt     ← Kotlin object ⇄ Firestore document
    │   │   ├── MujDemoSpots.kt        ← 4 demo spots near Manipal University Jaipur
    │   │   └── ParkoraRepository.kt   ← login, live listeners, bookings, admin functions
    │   ├── navigation/                ← Routes.kt, ParkoraNavGraph.kt
    │   ├── utils/                     ← BookingCalculator, DateTimeUtils, Validation, ImageUtils
    │   └── ui/
    │       ├── theme/                 ← colours, font, MaterialTheme
    │       ├── components/            ← reusable UI pieces
    │       └── screens/               ← auth/ home/ detail/ listing/ profile/ bookings/
    │                                    spots/ notifications/ admin/
    └── res/ (font, demo pictures, icons, ic_google.xml, values)
web/                                   ← the website (separate folder, same database)
```

**How the parts connect:**

```
MainActivity ─► ParkoraTheme ─► ParkoraNavGraph ─┬─ checking login → loading spinner
      │                                          ├─ nobody signed in → LoginScreen ("Continue with Google")
      │                                          └─ signed in → NavHost → Screens → Components
      │                                                                    │
      └─ ParkoraRepository.init() ──────────────────────────────────────► ParkoraRepository
                                                                            │  ▲ snapshot listeners (live)
                                                                            ▼  │
                                              Firebase Auth (Google)  +  Cloud Firestore  ◄── website
                                                                            ▲
                                                           firestore.rules (checked on Google's servers)
```

---

## 3. File-by-file explanation

### Project / build files

| File | What it does |
|---|---|
| `settings.gradle.kts` | Names the project, includes `:app`, lists the library repositories (Google, Maven Central). |
| `gradle/libs.versions.toml` | **Version catalog**: Compose BOM, Navigation, Coil, **Firebase BOM 34.19.0** (`firebase-auth`, `firebase-firestore`), **Credential Manager** + `googleid` (Google sign-in sheet), `kotlinx-coroutines-play-services` (`await()`). |
| `app/build.gradle.kts` | App settings (package `com.example.parkora`, minSdk 26, versionName 2.0) and `dependencies { }`. |
| `firebase.json`, `.firebaserc` | Tell the Firebase command-line tool which project and which rules file to deploy. |
| `firebase/firestore.rules` | The **security rules** (see section 3.9). Deploy: `npx firebase-tools deploy --only firestore:rules`. |
| `app/src/main/keepRules/rules.keep` | R8 keep rule for release builds. |
| `local.properties` | Path to the Android SDK on *your* PC (created by Android Studio, not shared). |

### 3.1 `AndroidManifest.xml`
Declares `android.permission.INTERNET` (needed for Firebase) and the single `MainActivity` with the
`MAIN`/`LAUNCHER` intent filter.

### 3.2 `MainActivity.kt` — entry point
`onCreate()` → `enableEdgeToEdge()` → `ParkoraRepository.init(applicationContext)` (connects to Firebase and
starts watching the login state) → `setContent { ParkoraTheme { ParkoraNavGraph() } }`.

### 3.3 `data/Models.kt` — data models
Kotlin **data classes**, one per Firestore collection:
- `User` – id (= Firebase uid), name, email, phone, location, createdAt, profileImagePath, **role** (`"user"`/`"admin"`), **isBlocked**. `isAdmin` is a computed property.
- `ParkingSpot` – grouped by the 4 listing steps (location, size, documents, pricing) + `isAvailable`, `isVerified`, `rating`, `bookedSlots`.
- `BookedSlot` – `bookingId`, `startMillis`, `endMillis`: a time range already taken on a spot (used for the double-booking check).
- `Booking` + `enum BookingStatus` (`PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED, EXPIRED`).
- `AppNotification` – in-app message for one user.

A "booking request" is **not a separate class**: it is a `Booking` whose `ownerId` is the signed-in user.

### 3.4 `data/FirebaseConfig.kt`
Project id, API key, Android app id, the OAuth **web client id** (needed so Google Sign-In returns an
ID token Firebase accepts) and **`ADMIN_EMAIL`**. These values identify the project; they are not passwords.

### 3.5 `data/FirestoreMapper.kt`
Firestore stores documents as maps. `User.toMap()`, `ParkingSpot.toMap()`, … convert our objects into maps
to save them; `DocumentSnapshot.toUser()`, `toParkingSpot()`, … convert documents back. Written by hand so the
field names (e.g. `isVerified`) are exactly the same as on the website.

### 3.6 `data/MujDemoSpots.kt`
Four demo listings near MUJ, owned by the admin and marked "Parkora (demo listing)":

| Id | Spot | Where | Price |
|---|---|---|---|
| `muj_demo_1` | DK-12 Dehmi Kalan Village Parking | Near MUJ Main Gate, Dehmi Kalan | ₹20/hr • ₹150/day |
| `muj_demo_2` | AE-04 Ajmer Road Student Residency | Jaipur–Ajmer Expressway (NH-48), 1 km from MUJ | ₹30/hr • ₹220/day |
| `muj_demo_3` | BG-21 Bagru Heights Apartments | Basement B1, Bagru Road, 3 km from MUJ | ₹25/hr • ₹180/day |
| `muj_demo_4` | TW-07 Dehmi Kalan Market Stand (two-wheelers) | Market Road, Dehmi Kalan | ₹10/hr • ₹60/day |

The admin adds them with **Admin Portal → Overview → Add MUJ demo spots**. Fixed ids = no duplicates.

### 3.7 `data/ParkoraRepository.kt` — the data layer (most important file)
A Kotlin `object`; the **only** place that reads or changes data.
- **State for Compose:** `parkingSpots`, `bookings`, `notifications`, `allUsers` (`mutableStateListOf`),
  `currentUser`, `isCheckingLogin`, `isLoadingSpots` (`mutableStateOf`).
- **`init()`** creates the `FirebaseApp` from `FirebaseConfig` and adds an **auth state listener**.
- **`onLoginChanged()`** → `loadOrCreateUser()` (first sign-in creates `users/{uid}` from the Google profile;
  role = admin only for `ADMIN_EMAIL`) → **`startListeners()`**:
  spots (all), bookings (mine + for my spots; admin: all), notifications (mine), users (admin only), my own user doc.
- **`signInWithGoogle(activityContext)`** – shows Android's Google account sheet (`CredentialManager` +
  `GetSignInWithGoogleOption`), gets a Google **ID token**, signs in to Firebase with `GoogleAuthProvider`.
  **`logout()`** – `auth.signOut()` + clears the remembered account.
- **Changing data** – `suspend` functions returning an **error message or `null`**: `updateProfile`,
  `updateLocation`, `updateProfilePhoto`, `addParkingSpot`, `setSpotAvailability`, `deleteSpot`,
  `createBooking`, `cancelBooking`, `respondToBooking`.
- **`createBooking()`** validates, then runs a **Firestore transaction**: re-reads the spot, checks its
  `bookedSlots` for overlap (`newStart < oldEnd && newEnd > oldStart`), adds the new slot, saves the booking
  and the owner's notification — all-or-nothing, safe if two people book at the same moment.
- **Cancel / reject** use a **batch write**: change the status, remove the slot (`FieldValue.arrayRemove`), notify.
- **`updateFinishedBookings()`** – Approved → Completed, Pending → Expired after check-out (runs automatically).
- **Admin functions:** `adminSetSpotVerified`, `adminDeleteSpot` (also cancels its bookings and notifies renters),
  `adminCancelBooking`, `adminSetUserBlocked`, `adminAddMujDemoSpots`.
- **`attempt { }`** wraps every write: 15-second timeout and friendly messages ("No internet connection",
  "You are not allowed to do that", …).

### 3.8 `navigation/` — `Routes.kt`, `ParkoraNavGraph.kt`
`ParkoraNavGraph()` chooses what to show: **loading spinner** while Firebase checks the saved login →
**`LoginScreen`** if nobody is signed in → otherwise `SignedInNavHost()` (a `NavHost` starting at `HOME`).
Because Login is shown whenever `currentUser == null`, **logout automatically returns to Login**.
Routes include `parking_detail/{spotId}` (argument) and `admin_portal`.

### 3.9 `firebase/firestore.rules` — security on the server
- `users`: you read/edit only your own profile (name, phone, location, photo); nobody can give themselves
  the admin role; only the admin can block/unblock.
- `parking_spots`: everyone can read; you create spots only as yourself and unverified; the owner may only
  pause/resume; any signed-in user may only change `bookedSlots` (the booking transaction); admin may do anything.
- `bookings`: only renter, owner and admin can read; new bookings must be PENDING and made by yourself;
  renter → CANCELLED, owner → APPROVED/REJECTED, after check-out → COMPLETED/EXPIRED; admin may do anything.
- `notifications`: read only your own; mark only your own as read.
- `isAdmin()` = signed-in user whose verified Google email is `vatsalsharma849@gmail.com`.

### 3.10 `utils/`
- `BookingCalculator.kt` – hours rounded **up**; **< 24 h → hourly**, otherwise **days (rounded up) × daily price**.
  Example from the synopsis: 22-09 7 AM → 30-09 11 PM = 208 h → 9 days × ₹15 = ₹135.
- `DateTimeUtils.kt` – formatting and `LocalDate + LocalTime ⇄ millis`.
- `Validation.kt` – name, email, 10-digit phone, required, positive number.
- `ImageUtils.kt` – `uriToDataUrl()` shrinks a picked photo (max 800 px, JPEG 70 %) to a
  `"data:image/jpeg;base64,…"` string so it fits in a Firestore document (max 1 MB); `dataUrlToBytes()`; `getFileName()`.

### 3.11 `ui/theme/` and `ui/components/`
Theme: colours from the screenshots, Work Sans font, `ParkoraTheme`. Components: `YellowHeader`, `YellowButton`,
`OutlineButton`, `ParkoraTextField`, `PickerField`, **`ParkingImage`** (demo picture / data-URL photo / https URL),
`VerifiedBadge`, `StatusBadge`, `CreamCard`, `EmptyState`, `ProfileMenuItem`, `SelectChip`, `ParkingCard`,
`BookingCard`, `ParkoraBottomBar`, `StepIndicator`, `TabSwitcher`.

### 3.12 `ui/screens/`
| Screen | What it does |
|---|---|
| `auth/LoginScreen.kt` | "Continue with Google" button (`ic_google.xml` logo), spinner, error text. |
| `home/HomeScreen.kt` | Figure 1: "Hey, name", location ✏, bell, **live** Latest Listed, loading spinner, bottom bar. |
| `home/AllParkingScreen.kt` | View All with search. |
| `detail/ParkingDetailScreen.kt` | Figure 5: details, date/time pickers, live price Summary, Book Now ("Booking…" while saving). |
| `listing/ListParkingScreen.kt` | Figure 3: 4-step form; up to 3 photos (compressed); document names; Submit publishes online. |
| `profile/ProfileScreen.kt` | Figure 2: Google photo or initials, menu, **Admin Portal** row for the admin, logout. |
| `profile/EditProfileScreen.kt` | Name, phone, location (email comes from Google, read-only). |
| `profile/InfoScreens.kt` | Contact Us, Privacy Policy (explains Firebase storage). |
| `bookings/MyBookingsScreen.kt` | Figure 4: Ongoing / Past, Cancel. |
| `bookings/BookingRequestsScreen.kt` | Owner: Approve / Reject; History. |
| `spots/MyParkingSpotsScreen.kt` | Owner: pause/resume, delete. |
| `notifications/NotificationsScreen.kt` | Live in-app notifications. |
| **`admin/AdminPortalScreen.kt`** | **Overview** (users, spots, awaiting verification, bookings, active, booking value, *Add MUJ demo spots*), **Spots** (verify / un-verify / remove), **Bookings** (cancel), **Users** (block / unblock). |

---

## 4. Key concepts explained with Parkora examples

**Activity** – Parkora has one: `MainActivity`; `onCreate()` calls `setContent { }`.

**Composable function** – `@Composable fun ParkingCard(spot, …)` draws a card; it re-runs (recomposes) when the data it reads changes.

**State** – `var isSigningIn by remember { mutableStateOf(false) }` in `LoginScreen`. Repository lists use
`mutableStateListOf`, so when Firestore sends a new spot, the Home screen redraws by itself.

**Coroutines** – network calls take time, so they are `suspend` functions. A screen calls them like:
```kotlin
val scope = rememberCoroutineScope()
scope.launch { bookingError = ParkoraRepository.createBooking(spot, checkIn, checkOut) }
```
`task.await()` (from `kotlinx-coroutines-play-services`) waits for a Firebase task without freezing the UI.

**Real-time listener** – `db.collection("parking_spots").addSnapshotListener { snap, _ -> … }` runs again every
time anyone (phone or website) changes a spot.

**Transaction** – reads and writes that succeed or fail **together** (`db.runTransaction { tx -> … }`), used for booking.

**Security rules** – code on Google's servers that allows or denies every request; the app cannot bypass them.

**Navigation** – `NavHost` + routes; arguments via `backStackEntry.arguments?.getString("spotId")`.

**Forms & validation** – value state + error state per field; the listing form validates one step at a time.

**Data class** – `booking.copy(status = BookingStatus.CANCELLED)`.

---

## 5. Project walkthrough (demo script, ~6 minutes)

Before the demo: the emulator/phone must have a Google account (Settings → Passwords & accounts → Add account).

1. **Launch** → yellow spinner → **Login**. Tap **Continue with Google** → choose the admin account
   `vatsalsharma849@gmail.com` → **Home**.
2. **Profile → Admin Portal → Overview** → **Add MUJ demo spots** → 4 spots appear on Home **instantly**.
3. Open **https://parkora-tan.vercel.app/parking/** on a laptop → the **same 4 spots** appear there (shared live database).
4. On a second device/browser sign in with **another Google account** → open **AE-04** → change dates → Summary
   switches hours → days → **Book Now** → "Booking requested!".
5. Back on the admin phone: the **bell** gets a red dot ("New booking request") → **Booking Requests** → **Approve**.
   The renter sees **Approved** in **My Bookings** immediately.
6. Try to book the **same time again** → "already booked for part of that time" (transaction check).
7. Renter: tap **+** → 4-step form → Submit → the listing is live with "Verification pending".
   Admin: **Admin Portal → Spots → Verify** → the green tick appears for everyone.
8. **Admin Portal → Users → Block** the renter → the renter can no longer list or book. Unblock again.
9. **Edit Profile**, **Notifications**, **Logout** → returns to Login.

---

## 6. Viva questions and answers

1. **What is Parkora?** A peer-to-peer parking app: owners list spare parking spaces; users find and book them for a date/time range.
2. **Language and UI toolkit?** Kotlin + Jetpack Compose (Material 3). No XML layouts or Fragments.
3. **How many Activities?** One – `MainActivity`; screens are composables switched by Navigation Compose.
4. **Where is the data stored?** In **Cloud Firestore** (Firebase), collections `users`, `parking_spots`, `bookings`, `notifications`.
5. **Why Firebase?** It is in the synopsis tech stack, it is free for this scale, gives Google login, a real-time database and server-side security rules without writing our own server.
6. **How does Google login work?** Credential Manager shows the Google account sheet → returns a Google **ID token** → `auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))` → Firebase user with a uid.
7. **Why do we need the SHA-1 fingerprint?** Google only issues ID tokens to an Android app whose package name and signing certificate (SHA-1) are registered in Firebase.
8. **What is the web client id for?** `GetSignInWithGoogleOption` needs the OAuth *web* client id so the token's audience is our Firebase project.
9. **Do we store passwords?** No. Google handles authentication; Parkora never sees the Google password.
10. **How does the app know who is admin?** `ADMIN_EMAIL` in `FirebaseConfig.kt` sets `role = "admin"` on first sign-in, and the **security rules** check the verified email on the server, so nobody can make themselves admin.
11. **What can the admin do?** See statistics, verify/un-verify or remove listings, cancel any booking, block/unblock users, add the MUJ demo spots.
12. **How do screens update when someone else changes data?** Snapshot listeners receive the change and replace the `mutableStateListOf` contents; Compose recomposes the screens.
13. **What is a `suspend` function?** A function that can pause (e.g. wait for the network) without blocking the UI thread; called from a coroutine (`scope.launch { }`).
14. **What does `await()` do?** Converts a Firebase `Task` into a suspending call that returns the result or throws the error.
15. **How do you prevent double booking?** In a Firestore **transaction**: re-read the spot, check `bookedSlots` for overlap, then add the slot + booking together. If two people book at once, Firestore retries one transaction, which then sees the clash.
16. **What is a batch write?** Several writes committed together (e.g. cancel = status + remove slot + notification).
17. **How is the price calculated?** Hours rounded up; < 24 h → hours × hourly price; otherwise days (rounded up) × daily price (`BookingCalculator`).
18. **Example?** 98 h → 5 days × ₹350 = ₹1750.
19. **Booking statuses?** Pending, Approved, Rejected, Cancelled, Completed, Expired.
20. **Who may change a booking's status?** Renter → Cancelled; owner → Approved/Rejected; after check-out → Completed/Expired; admin → anything. Enforced by the rules.
21. **How are photos stored?** Compressed to ≤ 800 px JPEG and saved as a Base64 data-URL string inside the spot document (Firebase Storage needs a paid plan). Max 3 photos so the document stays under 1 MB.
22. **How are images displayed?** Coil `AsyncImage`: drawable id for demo pictures, `ByteArray` for data-URLs, URL string for Google photos (needs `coil-network-okhttp`).
23. **Why `FirestoreMapper` instead of `toObject()`?** Kotlin `isVerified` would be saved as `verified` by the automatic mapper; manual maps keep field names identical to the website.
24. **What are security rules?** Server-side code that allows/denies every read and write; e.g. only the renter, owner and admin can read a booking.
25. **How does the website share data?** It uses the same Firebase project (`web/src/lib/firebase.ts`) and the same collection and field names.
26. **What happens on logout?** `auth.signOut()` → auth listener sets `currentUser = null` → `ParkoraNavGraph` shows `LoginScreen`.
27. **What does `isCheckingLogin` do?** Shows a spinner until Firebase knows if a saved login exists, so the login screen doesn't flash.
28. **Explain the 4-step listing form.** `currentStep` (1–4) decides which fields show; `validateStep()` must pass before `currentStep++`; Submit calls `addParkingSpot()` inside a coroutine.
29. **What is `BackHandler`?** In the listing form, the phone Back button goes to the previous step.
30. **Why minSdk 26?** `java.time` and variable fonts work without extra setup.
31. **Are notifications push notifications?** No – Firestore documents shown in the app in real time; FCM push is future scope.
32. **Limitations?** No payments, no maps/GPS, no push notifications, photos limited to 3 per spot, document files not uploaded, APK not on Play Store.
33. **Future scope?** Payments, maps & distance search, FCM push, ratings & reviews, Firebase Storage for documents, Play Store release, QR check-in.

---

## 7. Test report

**Verified (29 Oct 2026)**
- Android app and website **compile and build** with Firebase (no errors).
- Firebase project `parkora-muj-app` created; Android app (with debug SHA-1/SHA-256) and web app registered.
- Firestore database created; **security rules compiled and deployed** successfully.
- Live website https://parkora-tan.vercel.app connects to Firestore (public spot list loads, no console errors).

**Still to be tested by you (needs your Google account)**
- Google sign-in on the emulator/phone and on the website, the Admin Portal, adding the MUJ demo spots,
  booking between two different Google accounts, approve/reject, block/unblock.
- (Version 1 workflows – price calculation, listing form, validation – were tested on the emulator on 26 Oct 2026.)

---

## 8. How to open and run in Android Studio (Windows)

1. Android Studio → **File › Open** → `C:\Data from D drive\Parkora\android` → **Trust Project**.
2. Wait for **Gradle Sync** (needs internet the first time).
3. Start an emulator (Device Manager) **with Google Play**, and add a Google account in its Settings.
4. Press **Run ▶** → **Continue with Google**.

Important: Google Sign-In works with the debug key of **this laptop** (its SHA-1 is registered in Firebase).
On another computer, add that computer's SHA-1 in Firebase console → Project settings → Your apps.

**Deploy rules after editing them:** `npx firebase-tools deploy --only firestore:rules` (run inside `android/`).
**Website:** see `../web/README.md` (`npm run dev`, deploy with `npx vercel deploy --prod`).
