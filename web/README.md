# PARKORA Web — Smart Peer-to-Peer Parking & Booking

**Live:** https://parkora-tan.vercel.app

The website of the Parkora PBL project. It has the same yellow-and-white design, data models and rules as the
Android app in `../android`, and it uses the **same Firebase project (`parkora-muj-app`)** — so the website and the
Android app share **one live database** and the same Google accounts.

- **Login:** "Continue with Google" (Firebase Authentication). No Parkora passwords.
- **Database:** Cloud Firestore (`users`, `parking_spots`, `bookings`, `notifications`), real-time.
- **Admin:** `vatsalsharma849@gmail.com` gets the **Admin Portal** at `/admin` (stats, verify/remove listings,
  cancel bookings, block users, *Add MUJ demo spots*).
- **Security:** Firestore rules in `../android/firebase/firestore.rules`, checked on Google's servers.
- **Not included:** online payments, maps/GPS, push notifications. Photos are compressed and stored in Firestore;
  for documents only the file name is recorded.

---

## 1. Run it on your laptop

Requirements: **Node.js 20+** (tested with Node 24).

```bash
cd "C:\Data from D drive\Parkora\web"
npm install
npm run dev
```

Open **http://localhost:3000** (localhost is allowed for Google sign-in by default).

| Command | What it does |
|---|---|
| `npm run build` | Type-checks and builds the static site into `out/` |
| `npm run preview` | Serves `out/` at http://localhost:3000 |
| `npx vercel deploy --prod` | Publishes the site to https://parkora-tan.vercel.app |

Don't run `npm run build` while `npm run dev` is running (both use `.next`). If the dev server shows a
"React Client Manifest" error, stop it, delete `.next` and start again.

---

## 2. Important files

```
src/lib/
├── firebase.ts            ← Firebase connection + ADMIN_EMAIL
├── store.tsx              ← data layer (= ParkoraRepository.kt): Google login, live listeners,
│                            createBooking (transaction), approve/reject, admin functions
├── types.ts               ← data models (= Models.kt)
├── mujDemoSpots.ts        ← 4 demo spots near Manipal University Jaipur (= MujDemoSpots.kt)
├── bookingCalculator.ts   ← hourly / daily price rule
├── utils.ts, image.ts     ← validation, dates, photo compression
src/components/            ← Button, TextField, ParkingCard, BookingCard, PageHeader, Tabs, dialogs…
src/app/                   ← one folder = one page
├── page.tsx               ← /            landing page
├── login/                 ← /login       Continue with Google
├── parking/, parking/view ← discovery + details & booking (/parking/view?id=…)
├── list-spot/             ← 4-step listing form
├── my-spots/, bookings/, requests/, profile/, profile/edit/, notifications/, privacy/
└── admin/                 ← /admin       Admin Portal (admin account only)
```

`/parking/view?id=…` is used instead of `/parking/[id]` because the site is exported as static files and
cannot pre-build pages for spots that users add later.

---

## 3. Demo (with the Android app)

1. Sign in on the website as the admin → **Admin** (top bar) → **Add MUJ demo spots**.
2. Open the Android app → the same 4 spots near MUJ appear instantly.
3. Sign in with another Google account (other browser / phone) → book **AE-04** → the admin gets a notification
   and approves in **Requests** → the renter sees **Approved** live.
4. Booking the same time again is rejected (Firestore transaction).
5. List a spot (4 steps) → admin verifies it in **Admin → Spots**.

---

## 4. Firebase setup notes

- Google sign-in on the live site needs `parkora-tan.vercel.app` in **Firebase console → Authentication →
  Settings → Authorized domains**.
- Config values in `src/lib/firebase.ts` identify the project; they are not secrets.
