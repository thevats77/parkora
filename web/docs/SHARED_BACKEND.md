# Proposal: one backend for the Parkora website and Android app

**Status: IMPLEMENTED in version 2 (Firebase option, section 2).** Both apps now use the Firebase project `parkora-muj-app`; rules are in `android/firebase/firestore.rules`. The text below is the original proposal, kept for the report.

~~Status: proposal only.~~ Originally nothing here was configured or connected. The Android app
(`android/…/data/ParkoraRepository.kt`) saves data in SharedPreferences on the phone, and the website
(`web/src/lib/store.tsx`) saves data in the browser's localStorage. They do not share data today.

## 1. What already lines up

Both apps use **the same data models with the same field names**:

| Android (Kotlin) `Models.kt` | Website (TypeScript) `types.ts` | Synopsis table |
|---|---|---|
| `data class User` | `interface User` | `users` |
| `data class ParkingSpot` | `interface ParkingSpot` | `parking_spots` |
| `data class Booking` + `enum BookingStatus` | `interface Booking` + `type BookingStatus` | `bookings` / `booking_requests` |
| `data class AppNotification` | `interface AppNotification` | `notifications` |

Both apps also put **all data access behind one object with the same functions**
(`login`, `register`, `addParkingSpot`, `createBooking`, `cancelBooking`, `respondToBooking`, …).
The screens never touch storage directly, so only that one file has to change in each app.

## 2. Recommended option: Firebase (matches the synopsis)

| Need | Firebase service | Replaces |
|---|---|---|
| Accounts & login | **Firebase Authentication** (email/password) | local SHA-256 hashes (remove `passwordHash`) |
| Users, spots, bookings, notifications | **Cloud Firestore** collections `users`, `parking_spots`, `bookings`, `notifications` | SharedPreferences / localStorage |
| Parking photos & documents | **Firebase Storage** (`spots/{spotId}/…`, `documents/{spotId}/…`) | data URLs / app files |
| Push notifications | **Firebase Cloud Messaging** | in-app notification list |

Steps:
1. Create one Firebase project; add an **Android app** (package `com.example.parkora`) and a **Web app**.
2. Android: add the Firebase BoM + `firebase-auth`, `firebase-firestore`, `firebase-storage`; rewrite the
   body of each `ParkoraRepository` function to call Firestore (use coroutines / listeners for live updates).
3. Web: `npm install firebase`; rewrite the body of each function in `store.tsx` to call Firestore
   (`onSnapshot` keeps the React state live).
4. **Move the important rules to the server** so neither app can skip them — e.g. a Cloud Function or a
   Firestore transaction for `createBooking` (overlap check + price calculation), and Security Rules such as:
   - users can only edit their own profile and their own spots;
   - only the spot owner can change a booking from `PENDING` to `APPROVED`/`REJECTED`;
   - only the booking user can cancel, and only before check-in.
5. Use `serverTimestamp()` instead of each device's clock for `createdAt`.

## 3. Alternative: a custom REST API

The synopsis (§13) already lists the endpoints. A small Node/Express or Spring Boot server with
PostgreSQL/MySQL tables named like the collections above would work:

```
POST /auth/register   POST /auth/login           → returns a token (JWT)
GET  /parking-spots   GET /parking-spots/{id}    POST/PUT/DELETE /parking-spots/{id}
POST /bookings        GET /bookings/user/{id}    GET /bookings/owner/{id}
PATCH /bookings/{id}/status                      POST /media/upload
```

Android would call it with **Retrofit**, the website with `fetch`. The overlap check and price
calculation (`BookingCalculator`) would move into the server so both apps get identical results.

## 4. What would change in each app

- **Android:** only `ParkoraRepository.kt` (+ Gradle dependencies, `google-services.json`, INTERNET permission).
- **Website:** only `src/lib/store.tsx` (+ `firebase` package and a `.env.local` with the web config).
  The site could stay a static export, or use Next.js server routes if a REST API is chosen.
- Demo data would be loaded once into the database instead of into each device.
