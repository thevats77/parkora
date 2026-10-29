"use client";

import { Building2, Car, MapPin, Ruler, Star } from "lucide-react";
import Link from "next/link";
import type { Booking, ParkingSpot } from "@/lib/types";
import { cn, formatBookingDate, rupees } from "@/lib/utils";
import { ButtonLink, CreamCard, StatusBadge, VerifiedBadge } from "./ui";

/*
 * PARKING & BOOKING CARDS – web versions of ParkingCard.kt and BookingCard.kt.
 */

/** Shows a demo picture (from /public/images) or a photo the owner uploaded (stored as a data URL). */
export function ParkingImage({ path, alt, className }: { path?: string; alt: string; className?: string }) {
  const src = !path ? "/images/demo_parking_1.jpg" : path.startsWith("demo_") ? `/images/${path}.jpg` : path;
  // eslint-disable-next-line @next/next/no-img-element
  return <img src={src} alt={alt} loading="lazy" className={cn("object-cover", className)} />;
}

export const priceText = (s: ParkingSpot) => `${rupees(s.pricePerHour)}/hr • ${rupees(s.pricePerDay)}/day`;

export const availableForText = (s: ParkingSpot) =>
  [s.availableForRent && "Rent", s.availableForSale && "Sale"].filter(Boolean).join(", ");

export const spotDetailHref = (id: string) => `/parking/view/?id=${encodeURIComponent(id)}`;

/** Home / discovery card (Figure 1 of the synopsis). */
export function ParkingCard({ spot, index = 0 }: { spot: ParkingSpot; index?: number }) {
  return (
    <CreamCard as="article" className="group overflow-hidden transition duration-200 hover:-translate-y-1 hover:shadow-[0_10px_30px_rgb(254_215_0/0.35)] animate-fade-up"
    >
      <Link href={spotDetailHref(spot.id)} className="block" aria-label={`View parking ${spot.parkingNumber}`}>
        <div className="relative h-44 overflow-hidden">
          <ParkingImage path={spot.imagePaths[0]} alt={`Parking ${spot.parkingNumber}`}
            className="h-full w-full transition duration-500 group-hover:scale-105" />
          {spot.rating > 0 ? (
            <span className="absolute right-3 top-3 inline-flex items-center gap-1 rounded-full bg-white/95 px-2 py-0.5 text-xs font-semibold shadow">
              <Star className="size-3.5" fill="var(--color-star)" stroke="var(--color-star)" aria-hidden /> {spot.rating}.0
            </span>
          ) : (
            <span className="absolute right-3 top-3 rounded-full bg-white/95 px-2 py-0.5 text-xs font-semibold shadow">New</span>
          )}
        </div>
      </Link>
      <div className="p-5" style={{ animationDelay: `${index * 60}ms` }}>
        <div className="flex items-center gap-1.5">
          <h3 className="text-xl font-semibold">{spot.parkingNumber}</h3>
          {spot.isVerified ? <VerifiedBadge className="size-[18px]" /> : null}
        </div>
        <p className="mt-2 flex items-start gap-2 text-sm">
          <MapPin className="mt-0.5 size-4 shrink-0" aria-hidden /> {spot.address}, {spot.city}, India
        </p>
        <div className="mt-3 flex items-center justify-between gap-3">
          <div>
            <p className="text-lg font-semibold">{priceText(spot)}</p>
            <span className="mt-1.5 inline-block rounded bg-chip px-2 py-0.5 text-[11px] text-muted">For {availableForText(spot)}</span>
          </div>
          {spot.availableForRent ? <ButtonLink href={spotDetailHref(spot.id)} size="sm" arrow>Book Now</ButtonLink> : null}
        </div>
        <div className="mt-4 space-y-1.5 text-[13px]">
          <p className="flex items-center gap-4">
            <span className="flex items-center gap-1.5"><Ruler className="size-4" aria-hidden /> {spot.lengthFeet}×{spot.widthFeet} ft</span>
            <span className="flex min-w-0 items-center gap-1.5"><Building2 className="size-4 shrink-0" aria-hidden /> <span className="truncate">{spot.societyName}</span></span>
          </p>
          <p className="flex items-center gap-1.5"><Car className="size-4" aria-hidden /> {spot.vehicleTypes.join(", ")}</p>
        </div>
      </div>
    </CreamCard>
  );
}

/** One booking (Figure 4). `actions` lets each page add its own buttons. */
export function BookingCard({ booking, verified, actions }: { booking: Booking; verified: boolean; actions?: React.ReactNode }) {
  return (
    <article className="rounded-2xl border border-booking-border bg-cream p-4 shadow-[0_2px_10px_rgb(242_207_138/0.25)] animate-fade-up">
      <div className="flex gap-4">
        <Link href={spotDetailHref(booking.spotId)} className="shrink-0" aria-label={`View parking ${booking.parkingNumber}`}>
          <ParkingImage path={booking.spotImage} alt={`Parking ${booking.parkingNumber}`} className="size-[84px] rounded-lg" />
        </Link>
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <p className="flex items-center gap-1.5 text-lg font-medium">
              {booking.parkingNumber} {verified ? <VerifiedBadge className="size-4" /> : null}
            </p>
            <StatusBadge status={booking.status} />
          </div>
          <p className="mt-1 text-[13px]">Booking By: {booking.userName}</p>
          <p className="text-[13px]">{formatBookingDate(booking.checkInMillis)}</p>
          <p className="text-xs text-muted">Till {formatBookingDate(booking.checkOutMillis)} • {rupees(booking.totalPrice)}</p>
        </div>
      </div>
      {actions ? <div className="mt-3 flex flex-wrap justify-end gap-2.5">{actions}</div> : null}
    </article>
  );
}
