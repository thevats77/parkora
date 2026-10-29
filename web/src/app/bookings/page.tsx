"use client";

import { CalendarX2 } from "lucide-react";
import { useState } from "react";
import { ConfirmDialog, PageHeader, RequireLogin, Tabs, useToast } from "@/components/layout";
import { BookingCard } from "@/components/parking";
import { Button, ButtonLink, EmptyState, Skeleton } from "@/components/ui";
import { isOngoing, useParkora } from "@/lib/store";
import type { Booking } from "@/lib/types";
import { formatBookingDate } from "@/lib/utils";

/*
 * MY BOOKINGS ( /bookings ) – Figure 4
 *   Ongoing = Pending or Approved, Past = everything else.
 *   A booking can be cancelled until its check-in time.
 */
function MyBookings({ tab }: { tab: number }) {
  const { myBookings, data, canCancel, cancelBooking } = useParkora();
  const toast = useToast();
  const [toCancel, setToCancel] = useState<Booking | null>(null);

  const all = myBookings();
  const shown = tab === 0 ? all.filter(isOngoing) : all.filter((b) => !isOngoing(b)).reverse();
  const verified = (spotId: string) => !!data.parkingSpots.find((s) => s.id === spotId)?.isVerified;

  if (shown.length === 0) {
    return tab === 0
      ? <EmptyState icon={CalendarX2} title="No ongoing bookings" message="Find a spot and book it for the time you need."
          action={<ButtonLink href="/parking/" arrow>Find Parking</ButtonLink>} />
      : <EmptyState icon={CalendarX2} title="No past bookings yet" />;
  }

  return (
    <div className="mx-auto grid max-w-4xl gap-5 px-4 py-8 sm:px-6 md:grid-cols-2">
      {shown.map((b) => (
        <BookingCard key={b.id} booking={b} verified={verified(b.spotId)}
          actions={canCancel(b) ? <Button size="sm" arrow onClick={() => setToCancel(b)}>Cancel Booking</Button> : null} />
      ))}
      <ConfirmDialog open={!!toCancel} title="Cancel booking?" confirmText="Yes, cancel" danger
        message={toCancel ? <>Cancel your booking for <b>{toCancel.parkingNumber}</b> on {formatBookingDate(toCancel.checkInMillis)}?</> : null}
        onCancel={() => setToCancel(null)}
        onConfirm={async () => { const id = toCancel!.id; setToCancel(null); toast((await cancelBooking(id)) ?? "Booking cancelled"); }} />
    </div>
  );
}

export default function BookingsPage() {
  const [tab, setTab] = useState(0);
  return (
    <>
      <PageHeader title="My Bookings" back><Tabs tabs={["Ongoing", "Past"]} selected={tab} onSelect={setTab} /></PageHeader>
      <RequireLogin loading={<div className="mx-auto grid max-w-4xl gap-5 px-4 py-8 md:grid-cols-2"><Skeleton className="h-36" /><Skeleton className="h-36" /></div>}>
        <MyBookings tab={tab} />
      </RequireLogin>
    </>
  );
}
