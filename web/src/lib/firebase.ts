import { initializeApp, getApps } from "firebase/app";
import { getAuth, GoogleAuthProvider } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

/*
 * FIREBASE CONNECTION (website)
 * -----------------------------
 * The SAME Firebase project as the Android app ("parkora-muj-app"), so the website
 * and the app share one live database and the same Google accounts.
 * These values identify the project; they are not secret. Security comes from
 * the Firestore rules in android/firebase/firestore.rules (run on Google's servers).
 */
export const firebaseConfig = {
  apiKey: "AIzaSyBNm3akZ4F4UUyGH3svxCsgNK1HwDfJ04w",
  authDomain: "parkora-muj-app.firebaseapp.com",
  projectId: "parkora-muj-app",
  appId: "1:902881672311:web:b2f533554bd75aa8039a72",
};

/** The Google account that gets the Admin Portal (same as Android FirebaseConfig.ADMIN_EMAIL). */
export const ADMIN_EMAIL = "vatsalsharma849@gmail.com";

export const isFirebaseConfigured = !firebaseConfig.apiKey.startsWith("PASTE");

const app = getApps()[0] ?? initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({ prompt: "select_account" }); // always show the account chooser
