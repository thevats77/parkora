"use client";

import { BadgeCheck, CalendarClock, ClipboardCheck, Calculator, FileUp, Info, MapPinned, ShieldCheck, Wallet } from "lucide-react";
import Link from "next/link";
import { Logo } from "@/components/layout";
import { ParkingCard } from "@/components/parking";
import { ButtonLink, CardListSkeleton, CreamCard, EmptyState, StatusBadge } from "@/components/ui";
import { useParkora } from "@/lib/store";

/*
 * LANDING PAGE  ( / )
 * Introduces Parkora, explains how it works for drivers and owners,
 * and shows the latest listed parking spots from the (demo) data.
 */
export default function LandingPage() {
  const { ready, latestSpots, currentUser } = useParkora();
  const spots = ready ? latestSpots() : [];
  const heroSpot = spots.find((s) => s.id === "muj_demo_2") ?? spots[0];

  return (
    <>
      {/* ---------- Hero ---------- */}
      <section className="yellow-circles overflow-hidden rounded-b-[48px]">
        <div className="mx-auto grid max-w-6xl items-center gap-12 px-4 pb-16 pt-12 sm:px-6 md:grid-cols-[1.1fr_0.9fr] md:pb-24 md:pt-20">
          <div className="animate-fade-up">
            <span className="inline-flex items-center gap-2 rounded-full bg-white/70 px-3 py-1 text-xs font-semibold">
              <span className="size-2 rounded-full bg-verified" /> Smart Peer-to-Peer Parking & Booking
            </span>
            <h1 className="mt-5 text-4xl font-semibold leading-[1.08] tracking-tight sm:text-5xl lg:text-6xl">
              Park smarter.<br />Share your spare space.
            </h1>
            <p className="mt-5 max-w-lg text-lg text-navy/80">
              Parkora connects drivers who need a spot with residents and societies who have one free —
              hourly or daily, booked in a few taps.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link href="/parking/" className="inline-flex h-[54px] items-center gap-2 rounded-xl bg-navy px-6 font-bold text-white transition hover:bg-navy/90">
                Find Parking <MapPinned className="size-5" aria-hidden />
              </Link>
              <Link href={currentUser ? "/list-spot/" : "/login/?next=/list-spot/"}
                className="inline-flex h-[54px] items-center gap-2 rounded-xl border-2 border-navy px-6 font-bold transition hover:bg-white/50">
                List Your Spot
              </Link>
            </div>
            <dl className="mt-10 grid max-w-md grid-cols-3 gap-4 text-sm">
              {[["Hourly", "& daily pricing"], ["4-step", "spot listing"], ["Owner", "approves requests"]].map(([a, b]) => (
                <div key={a}><dt className="text-xl font-bold">{a}</dt><dd className="text-navy/70">{b}</dd></div>
              ))}
            </dl>
          </div>

          {/* Hero visual: a real parking card + floating booking summary */}
          <div className="relative mx-auto w-full max-w-sm animate-fade-up [animation-delay:120ms]">
            {heroSpot ? (
              <div className="rotate-[2deg] transition duration-300 hover:rotate-0"><ParkingCard spot={heroSpot} /></div>
            ) : ready ? null : (
              <div className="h-[420px] animate-pulse rounded-2xl bg-white/50" />
            )}
            <CreamCard className="absolute -bottom-8 -left-4 w-56 -rotate-[3deg] p-4 sm:-left-12">
              <p className="text-sm font-semibold">Summary</p>
              <div className="mt-2 flex justify-between text-xs"><span className="text-muted">Item Total</span><span className="font-medium">2 Day(s)</span></div>
              <div className="mt-1 flex justify-between text-xs"><span className="text-muted">2 × ₹350/day</span><span className="font-semibold">₹700</span></div>
              <div className="mt-3"><StatusBadge status="APPROVED" /></div>
            </CreamCard>
          </div>
        </div>
      </section>

      {/* ---------- How it works ---------- */}
      <section className="mx-auto max-w-6xl px-4 py-20 sm:px-6">
        <h2 className="text-center text-3xl font-semibold tracking-tight">How Parkora works</h2>
        <p className="mx-auto mt-2 max-w-xl text-center text-muted">Two sides of one marketplace — the same workflow as the Parkora Android app.</p>
        <div className="mt-12 grid gap-6 md:grid-cols-2">
          <HowColumn title="For drivers" steps={[
            [MapPinned, "Discover", "Browse verified spots with price, size and suitable vehicles."],
            [CalendarClock, "Pick date & time", "Choose check-in and check-out; the price is calculated instantly."],
            [ClipboardCheck, "Book & track", "Follow your request from Pending to Approved in My Bookings."],
          ]} />
          <HowColumn title="For parking owners" steps={[
            [FileUp, "List in 4 steps", "Location, parking size, photos & documents, then pricing."],
            [BadgeCheck, "Review requests", "Approve or reject booking requests for your spots."],
            [Wallet, "Earn from idle space", "Set hourly and daily rates and pause bookings any time."],
          ]} />
        </div>
      </section>

      {/* ---------- Latest listed ---------- */}
      <section className="bg-cream/60 py-20">
        <div className="mx-auto max-w-6xl px-4 sm:px-6">
          <div className="flex items-end justify-between gap-4">
            <div>
              <h2 className="text-3xl font-semibold tracking-tight">Latest Listed</h2>
              <p className="mt-1 text-muted">Recently listed parking spaces around Manipal University Jaipur.</p>
            </div>
            <ButtonLink href="/parking/" variant="outline" arrow>View All</ButtonLink>
          </div>
          <div className="mt-8">
            {ready ? (
              <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                {spots.slice(0, 3).map((s, i) => <ParkingCard key={s.id} spot={s} index={i} />)}
                {spots.length === 0 ? <div className="sm:col-span-3"><EmptyState title="No parking spots listed yet" message="Be the first to list one." /></div> : null}
              </div>
            ) : <CardListSkeleton />}
          </div>
        </div>
      </section>

      {/* ---------- Features ---------- */}
      <section className="mx-auto grid max-w-6xl gap-6 px-4 py-20 sm:grid-cols-2 sm:px-6 lg:grid-cols-4">
        {([
          [Calculator, "Automatic pricing", "Under 24 hours is charged per hour, longer stays per day."],
          [ShieldCheck, "No double booking", "Overlapping requests for the same spot are blocked."],
          [CalendarClock, "Ongoing & past", "Every booking keeps its status: pending, approved, completed…"],
          [BadgeCheck, "Owner dashboard", "My Parking Spots and Booking Requests in one place."],
        ] as const).map(([Icon, title, text]) => (
          <div key={title} className="rounded-2xl border border-card-border p-6 transition hover:shadow-[0_8px_24px_rgb(254_215_0/0.25)]">
            <span className="grid size-11 place-items-center rounded-xl bg-yellow"><Icon className="size-5" aria-hidden /></span>
            <h3 className="mt-4 font-semibold">{title}</h3>
            <p className="mt-1 text-sm text-muted">{text}</p>
          </div>
        ))}
      </section>

      {/* ---------- Live platform note ---------- */}
      <section className="mx-auto max-w-6xl px-4 sm:px-6">
        <CreamCard className="flex flex-col gap-4 p-6 sm:flex-row sm:items-center">
          <Info className="size-6 shrink-0" aria-hidden />
          <div className="flex-1 text-sm">
            <p className="font-semibold">Live on web and Android</p>
            <p className="text-muted">
              Sign in with Google. Listings, bookings and notifications are stored online (Google Firebase) and are
              shared instantly between this website and the Parkora Android app. Payments are made directly to the
              owner – online payments, maps and push notifications are not part of this version.
            </p>
          </div>
          {!currentUser ? <ButtonLink href="/login/" size="sm" arrow>Sign in</ButtonLink> : null}
        </CreamCard>
      </section>

      <footer className="mx-auto mt-16 flex max-w-6xl flex-col items-center justify-between gap-4 border-t border-card-border px-4 py-8 text-sm text-muted sm:flex-row sm:px-6">
        <Logo />
        <p>PBL Project · Smart Peer-to-Peer Parking & Booking · Vatsal Sharma (2427030235)</p>
        <Link href="/privacy/" className="hover:text-navy">Privacy</Link>
      </footer>
    </>
  );
}

function HowColumn({ title, steps }: { title: string; steps: [React.ComponentType<{ className?: string }>, string, string][] }) {
  return (
    <CreamCard className="p-6 sm:p-8">
      <h3 className="text-xl font-semibold">{title}</h3>
      <ol className="mt-6 space-y-5">
        {steps.map(([Icon, name, text], i) => (
          <li key={name} className="flex gap-4">
            <span className="relative grid size-11 shrink-0 place-items-center rounded-full bg-navy text-yellow">
              <Icon className="size-5" />
              <span className="absolute -right-1 -top-1 grid size-5 place-items-center rounded-full bg-yellow text-[11px] font-bold text-navy">{i + 1}</span>
            </span>
            <div><p className="font-semibold">{name}</p><p className="text-sm text-muted">{text}</p></div>
          </li>
        ))}
      </ol>
    </CreamCard>
  );
}
