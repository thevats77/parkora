"use client";

import { Inbox } from "lucide-react";
import { useState } from "react";
import { PageHeader, RequireLogin, Tabs, useToast } from "@/components/layout";
import { BookingCard } from "@/components/parking";
import { Button, EmptyState, Skeleton } from "@/components/ui";
import { useParkora } from "@/lib/store";

/*
 * BOOKING REQUESTS ( /requests ) – owner dashboard
 * Bookings other people made for MY spots. Pending ones can be approved or rejected.
 */
function Requests({ tab }: { tab: number }) {
  const { bookingRequestsForMe, respondToBooking, data } = useParkora();
  const toast = useToast();
  const all = bookingRequestsForMe();
  const shown = all.filter((b) => (tab === 0 ? b.status === "PENDING" : b.status !== "PENDING"));
  const verified = (id: string) => !!data.parkingSpots.find((s) => s.id === id)?.isVerified;

  if (shown.length === 0) {
    return <EmptyState icon={Inbox} title={tab === 0 ? "No new booking requests" : "No answered requests yet"}
      message={tab === 0 ? "Requests for your parking spots will appear here." : undefined} />;
  }

  return (
    <div className="mx-auto grid max-w-4xl gap-5 px-4 py-8 sm:px-6 md:grid-cols-2">
      {shown.map((b) => (
        <BookingCard key={b.id} booking={b} verified={verified(b.spotId)} actions={b.status === "PENDING" ? (
          <>
            <Button size="sm" variant="danger" onClick={async () => toast((await respondToBooking(b.id, false)) ?? "Request rejected")}>Reject</Button>
            <Button size="sm" arrow onClick={async () => toast((await respondToBooking(b.id, true)) ?? "Booking approved")}>Approve</Button>
          </>
        ) : null} />
      ))}
    </div>
  );
}

function PendingTabs({ tab, setTab }: { tab: number; setTab: (n: number) => void }) {
  const { ready, currentUser, bookingRequestsForMe } = useParkora();
  const pending = ready && currentUser ? bookingRequestsForMe().filter((b) => b.status === "PENDING").length : 0;
  return <Tabs tabs={[`Pending (${pending})`, "History"]} selected={tab} onSelect={setTab} />;
}

export default function RequestsPage() {
  const [tab, setTab] = useState(0);
  return (
    <>
      <PageHeader title="Booking Requests" back><PendingTabs tab={tab} setTab={setTab} /></PageHeader>
      <RequireLogin loading={<div className="mx-auto max-w-4xl px-4 py-8"><Skeleton className="h-36 md:w-1/2" /></div>}>
        <Requests tab={tab} />
      </RequireLogin>
    </>
  );
}
