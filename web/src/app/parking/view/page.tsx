"use client";

import { ArrowLeft, CalendarCheck, Car, ChevronDown, LogIn, LogOut, MapPin, Ruler, Star, UserRound } from "lucide-react";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useState } from "react";
import { ConfirmDialog } from "@/components/layout";
import { availableForText, ParkingImage, priceText } from "@/components/parking";
import { Button, ButtonLink, CreamCard, EmptyState, ErrorText, Skeleton, VerifiedBadge } from "@/components/ui";
import { calculatePrice, durationText } from "@/lib/bookingCalculator";
import { useParkora } from "@/lib/store";
import { cn, nextFullHour, rupees, toDateInput, toMillis, toTimeInput } from "@/lib/utils";

/*
 * PARKING DETAILS & BOOKING ( /parking/view?id=spot_p5_1456 )  – Figure 5 of the synopsis.
 * The id is read from the URL so the page also works for spots added later
 * (a static website cannot pre-build a page for every future spot).
 * The Summary box recalculates the price every time a date or time changes.
 */
function SpotDetails() {
  const id = useSearchParams().get("id") ?? "";
  const router = useRouter();
  const { ready, getSpot, currentUser, createBooking } = useParkora();

  // default booking: next full hour, for 2 hours
  const [start] = useState(nextFullHour);
  const end = new Date(start.getTime() + 2 * 3600_000);
  const [inDate, setInDate] = useState(toDateInput(start));
  const [inTime, setInTime] = useState(toTimeInput(start));
  const [outDate, setOutDate] = useState(toDateInput(end));
  const [outTime, setOutTime] = useState(toTimeInput(end));
  const [summaryOpen, setSummaryOpen] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [booked, setBooked] = useState(false);
  const [photo, setPhoto] = useState(0);
  const [booking, setBooking] = useState(false);

  if (!ready) {
    return (
      <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6" aria-busy="true">
        <Skeleton className="h-80 w-full rounded-3xl" />
        <div className="mt-6 grid gap-6 md:grid-cols-2"><Skeleton className="h-40" /><Skeleton className="h-64" /></div>
      </div>
    );
  }

  const spot = getSpot(id);
  if (!spot) {
    return <EmptyState icon={MapPin} title="Parking spot not found" message="It may have been removed by its owner."
      action={<ButtonLink href="/parking/" arrow>Browse parking</ButtonLink>} />;
  }

  const checkIn = toMillis(inDate, inTime);
  const checkOut = toMillis(outDate, outTime);
  const summary = calculatePrice(checkIn, checkOut, spot.pricePerHour, spot.pricePerDay);
  const isOwn = currentUser?.id === spot.ownerId;
  const today = toDateInput(new Date());
  const change = (setter: (v: string) => void) => (e: React.ChangeEvent<HTMLInputElement>) => { setter(e.target.value); setError(null); };

  async function book() {
    if (!currentUser) { router.push(`/login/?next=${encodeURIComponent(`/parking/view/?id=${id}`)}`); return; }
    setBooking(true);
    const err = await createBooking(spot!, checkIn, checkOut);
    setBooking(false);
    if (err) setError(err); else setBooked(true);
  }

  let blockedReason: string | null = null;
  if (isOwn) blockedReason = "This is your own parking spot.";
  else if (!spot.availableForRent) blockedReason = `This spot is listed for sale only. Contact the owner: ${spot.ownerName}.`;
  else if (!spot.isAvailable) blockedReason = "The owner has paused bookings for this spot.";

  return (
    <div className="mx-auto max-w-6xl px-0 pb-8 sm:px-6 sm:pt-6">
      {/* Photo */}
      <div className="relative">
        <ParkingImage path={spot.imagePaths[photo]} alt={`Parking ${spot.parkingNumber}`}
          className="h-72 w-full rounded-b-[22px] sm:h-[420px] sm:rounded-3xl animate-pop" />
        <button onClick={() => router.back()} aria-label="Go back"
          className="absolute left-4 top-4 grid size-12 place-items-center rounded-full bg-white shadow-lg transition hover:scale-105">
          <ArrowLeft className="size-6" aria-hidden />
        </button>
        {spot.imagePaths.length > 1 ? (
          <div className="absolute bottom-4 left-4 flex gap-2">
            {spot.imagePaths.map((p, i) => (
              <button key={i} onClick={() => setPhoto(i)} aria-label={`Photo ${i + 1}`}
                className={cn("overflow-hidden rounded-lg border-2", i === photo ? "border-yellow" : "border-white/70")}>
                <ParkingImage path={p} alt="" className="size-14" />
              </button>
            ))}
          </div>
        ) : null}
      </div>

      <div className="mt-6 grid gap-8 px-4 sm:px-0 lg:grid-cols-[1fr_420px]">
        {/* Info */}
        <section className="animate-fade-up">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h1 className="flex items-center gap-2 text-3xl font-bold sm:text-4xl">
              {spot.parkingNumber} {spot.isVerified ? <VerifiedBadge className="size-7" /> : null}
            </h1>
            <span className="rounded bg-yellow px-3 py-1.5 text-xs">Available For <b>{availableForText(spot)}</b></span>
          </div>
          {!spot.isVerified ? <p className="mt-1 text-xs text-muted">Verification pending</p> : null}
          <div className="mt-3 flex flex-wrap items-center justify-between gap-2">
            <p className="flex items-center gap-1.5 font-medium"><MapPin className="size-5" aria-hidden /> {spot.address}, {spot.city}, India</p>
            <Rating value={spot.rating} />
          </div>
          <div className="mt-3 flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm text-muted">Suitable for {spot.vehicleTypes.join(", ")}</p>
            <p className="text-lg font-medium">{priceText(spot)}</p>
          </div>

          <div className="mt-8 grid gap-3 sm:grid-cols-3">
            <InfoTile icon={Ruler} label="Size" value={`${spot.lengthFeet} × ${spot.widthFeet} ft · ${spot.parkingType}`} />
            <InfoTile icon={Car} label="Vehicles" value={spot.vehicleTypes.join(", ")} />
            <InfoTile icon={UserRound} label="Owner" value={`${spot.ownerName} · ${spot.societyName}`} />
          </div>
          <CreamCard className="mt-6 p-5 text-sm text-muted">
            <p className="font-semibold text-navy">How pricing works</p>
            Stays shorter than 24 hours are charged per started hour ({rupees(spot.pricePerHour)}/hr).
            Longer stays are charged per started day ({rupees(spot.pricePerDay)}/day).
            Payment is made directly to the owner — there is no online payment in this version.
          </CreamCard>
        </section>

        {/* Booking panel */}
        <aside className="lg:sticky lg:top-24 lg:self-start animate-fade-up [animation-delay:80ms]">
          <div className="rounded-3xl border border-card-border bg-white p-5 shadow-[0_10px_40px_rgb(0_0_0/0.06)]">
            <div className="grid grid-cols-2 gap-3">
              <DateTimeField label="Check-In Date" icon={LogIn} type="date" value={inDate} min={today} onChange={change(setInDate)} />
              <DateTimeField label="Check-In Time" icon={CalendarCheck} type="time" value={inTime} onChange={change(setInTime)} />
              <DateTimeField label="Check-Out Date" icon={LogOut} type="date" value={outDate} min={inDate} onChange={change(setOutDate)} />
              <DateTimeField label="Check-Out Time" icon={CalendarCheck} type="time" value={outTime} onChange={change(setOutTime)} />
            </div>

            <CreamCard className="mt-5 p-4">
              <button onClick={() => setSummaryOpen(!summaryOpen)} aria-expanded={summaryOpen}
                className="flex w-full cursor-pointer items-center justify-between text-lg font-semibold">
                Summary <ChevronDown className={cn("size-5 transition", summaryOpen && "rotate-180")} aria-hidden />
              </button>
              {summaryOpen ? (
                summary ? (
                  <dl className="mt-3 space-y-2 text-sm animate-fade-up" aria-live="polite">
                    <Row label="Duration" value={durationText(summary.totalHours)} />
                    <Row label="Item Total" value={`${summary.billedUnits} ${summary.unitName}`} />
                    <Row label={`Base Price (${summary.billedUnits} × ₹${summary.unitPrice}.00/${summary.shortUnit})`} value={`₹${summary.totalPrice}.00`} />
                    <div className="border-t border-card-border pt-2"><Row label="Total Amount" value={`₹${summary.totalPrice}.00`} bold /></div>
                  </dl>
                ) : <p className="mt-3 text-sm text-rejected" role="alert">Check-out must be after check-in.</p>
              ) : null}
            </CreamCard>

            {blockedReason ? <p className="mt-4 text-sm text-muted">{blockedReason}</p> : null}
            <ErrorText error={error} />
            <Button size="lg" arrow loading={booking} className="mt-4 w-full" disabled={!!blockedReason} onClick={book}>
              {currentUser ? "Book Now" : "Log in to Book"}
            </Button>
          </div>
        </aside>
      </div>

      <ConfirmDialog open={booked} title="Booking requested!" confirmText="View My Bookings" cancelText="OK"
        message={<>Your request for <b>{spot.parkingNumber}</b> was sent to {spot.ownerName}. It shows as “Pending” in My Bookings until the owner approves it.</>}
        onConfirm={() => router.push("/bookings/")} onCancel={() => setBooked(false)} />
    </div>
  );
}

function DateTimeField({ label, icon: Icon, ...input }: { label: string; icon: React.ComponentType<{ className?: string }> } & React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <label className="block">
      <span className="mb-1.5 ml-1 block text-[13px] text-muted">{label}</span>
      <span className="flex h-[52px] items-center rounded-[10px] border border-field-border bg-cream px-3 focus-within:shadow-[0_0_0_3px_rgb(254_215_0/0.35)]">
        <Icon className="size-[18px] shrink-0" />
        <span className="mx-2.5 h-6 w-px bg-[#e6e1c8]" />
        <input {...input} required className="min-w-0 flex-1 bg-transparent text-sm outline-none" />
      </span>
    </label>
  );
}

function Row({ label, value, bold }: { label: string; value: string; bold?: boolean }) {
  return (
    <div className={cn("flex justify-between gap-4", bold && "font-bold")}>
      <dt>{label}</dt><dd className={bold ? "" : "font-medium"}>{value}</dd>
    </div>
  );
}

function InfoTile({ icon: Icon, label, value }: { icon: React.ComponentType<{ className?: string }>; label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-card-border p-4">
      <Icon className="size-5" />
      <p className="mt-2 text-xs text-muted">{label}</p>
      <p className="text-sm font-medium">{value}</p>
    </div>
  );
}

function Rating({ value }: { value: number }) {
  if (!value) return <span className="text-xs font-bold">New</span>;
  return (
    <span className="flex" aria-label={`Rated ${value} out of 5`}>
      {[1, 2, 3, 4, 5].map((n) => (
        <Star key={n} className="size-4" stroke="var(--color-star)" fill={n <= value ? "var(--color-star)" : "none"} aria-hidden />
      ))}
    </span>
  );
}

// useSearchParams() must be inside <Suspense> when the site is exported as static files
export default function SpotDetailsPage() {
  return <Suspense><SpotDetails /></Suspense>;
}
