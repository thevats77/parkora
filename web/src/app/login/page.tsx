"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";
import { AuthShell } from "@/components/auth";
import { ErrorText } from "@/components/ui";
import { isFirebaseConfigured } from "@/lib/firebase";
import { useParkora } from "@/lib/store";

/*
 * LOGIN PAGE ( /login ) – "Continue with Google"
 * Real sign-in with Firebase Authentication. There are no Parkora passwords.
 * The first sign-in creates your profile in the shared database (the same account works
 * in the Android app). The admin account signs in the same way.
 */
function LoginForm() {
  const { signInWithGoogle, currentUser, ready } = useParkora();
  const router = useRouter();
  const next = useSearchParams().get("next") || "/parking/";
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Already signed in (or just signed in) -> continue to where the user wanted to go
  useEffect(() => {
    if (ready && currentUser) router.replace(next.startsWith("/") ? next : "/parking/");
  }, [ready, currentUser, next, router]);

  async function handleGoogle() {
    setLoading(true);
    setError(null);
    const err = await signInWithGoogle();
    setLoading(false);
    if (err) setError(err);
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold">Welcome!</h1>
      <p className="pb-6 text-muted">Find, book and list parking spaces around Manipal University Jaipur.</p>

      <button
        onClick={handleGoogle}
        disabled={loading}
        className="flex h-14 w-full cursor-pointer items-center justify-center gap-3 rounded-2xl border border-card-border bg-white font-semibold shadow-[0_2px_8px_rgb(0_0_0/0.08)] transition hover:shadow-md disabled:opacity-60"
      >
        {loading ? (
          <span className="size-5 animate-spin rounded-full border-2 border-navy/30 border-t-navy" aria-hidden />
        ) : (
          <GoogleLogo />
        )}
        {loading ? "Signing in…" : "Continue with Google"}
      </button>
      <ErrorText error={error} />

      {!isFirebaseConfigured ? (
        <p className="mt-4 rounded-xl bg-cream p-3 text-xs text-muted">Firebase is not connected yet – fill in web/src/lib/firebase.ts.</p>
      ) : null}

      <p className="mt-8 text-xs leading-5 text-muted">
        By continuing you agree that Parkora stores your Google name, email and photo, and your listings and bookings,
        in its online database (Google Firebase). Your Google password is never shared with Parkora.
      </p>
    </div>
  );
}

function GoogleLogo() {
  return (
    <svg viewBox="0 0 48 48" className="size-6" aria-hidden>
      <path fill="#FFC107" d="M43.6 20.5H42V20H24v8h11.3C33.7 32.7 29.2 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.8 1.2 7.9 3.1l5.7-5.7C34 6.1 29.3 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 20-8.9 20-20c0-1.3-.1-2.4-.4-3.5z" />
      <path fill="#FF3D00" d="m6.3 14.7 6.6 4.8C14.7 15.1 19 12 24 12c3.1 0 5.8 1.2 7.9 3.1l5.7-5.7C34 6.1 29.3 4 24 4 16.3 4 9.7 8.3 6.3 14.7z" />
      <path fill="#4CAF50" d="M24 44c5.2 0 9.9-2 13.4-5.2l-6.2-5.2C29.2 35.1 26.7 36 24 36c-5.2 0-9.6-3.3-11.3-8l-6.5 5C9.5 39.6 16.2 44 24 44z" />
      <path fill="#1976D2" d="M43.6 20.5H42V20H24v8h11.3c-.8 2.2-2.2 4.2-4.1 5.6l6.2 5.2C37 39.2 44 34 44 24c0-1.3-.1-2.4-.4-3.5z" />
    </svg>
  );
}

export default function LoginPage() {
  return (
    <AuthShell subtitle="Smart Peer-to-Peer Parking & Booking">
      <Suspense><LoginForm /></Suspense>
    </AuthShell>
  );
}
