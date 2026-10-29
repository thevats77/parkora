"use client";

import { BadgeCheck, CalendarCheck2, IndianRupee, MapPinPlus, ShieldAlert, Users } from "lucide-react";
import Link from "next/link";
import { useState } from "react";
import { ConfirmDialog, PageHeader, RequireLogin, Tabs, useToast } from "@/components/layout";
import { BookingCard, ParkingImage, priceText, spotDetailHref } from "@/components/parking";
import { Button, CreamCard, EmptyState, VerifiedBadge } from "@/components/ui";
import { isOngoing, useParkora } from "@/lib/store";
import type { ParkingSpot } from "@/lib/types";
import { rupees } from "@/lib/utils";

/*
 * ADMIN PORTAL ( /admin ) – only for the admin account (ADMIN_EMAIL in lib/firebase.ts)
 *   Overview – live numbers + "Add MUJ demo spots"
 *   Spots    – verify / un-verify, remove listings
 *   Bookings – every booking, cancel active ones
 *   Users    – everyone who signed in, block / unblock
 * The Firestore security rules check the admin email on Google's servers too.
 */
function AdminPortal() {
  const store = useParkora();
  const { data, isAdmin } = store;
  const toast = useToast();
  const [tab, setTab] = useState(0);
  const [toRemove, setToRemove] = useState<ParkingSpot | null>(null);
  const [busy, setBusy] = useState(false);

  if (!isAdmin) {
    return <EmptyState icon={ShieldAlert} title="Admins only" message="Only the Parkora admin account can open this page." />;
  }

  /** Runs an admin action and shows the result as a toast. */
  async function run(success: string, action: () => Promise<string | null>) {
    setBusy(true);
    const error = await action();
    setBusy(false);
    toast(error ?? success);
  }

  const spots = [...data.parkingSpots].sort((a, b) => Number(a.isVerified) - Number(b.isVerified) || b.createdAt - a.createdAt);
  const bookings = [...data.bookings].sort((a, b) => b.createdAt - a.createdAt);
  const value = bookings.filter((b) => b.status === "APPROVED" || b.status === "COMPLETED").reduce((sum, b) => sum + b.totalPrice, 0);

  const stats = [
    { icon: Users, label: "Users", value: data.users.length },
    { icon: MapPinPlus, label: "Parking spots", value: spots.length },
    { icon: BadgeCheck, label: "Awaiting verification", value: spots.filter((s) => !s.isVerified).length },
    { icon: CalendarCheck2, label: "Bookings", value: bookings.length },
    { icon: CalendarCheck2, label: "Active bookings", value: bookings.filter(isOngoing).length },
    { icon: IndianRupee, label: "Booking value", value: rupees(value) },
  ];

  return (
    <>
      <div className="mx-auto max-w-5xl px-4 sm:px-6"><Tabs tabs={["Overview", "Spots", "Bookings", "Users"]} selected={tab} onSelect={setTab} /></div>
      <div className="mx-auto max-w-5xl px-4 py-8 sm:px-6">
        {tab === 0 && (
          <div className="animate-fade-up">
            <div className="grid grid-cols-2 gap-4 md:grid-cols-3">
              {stats.map(({ icon: Icon, label, value }) => (
                <CreamCard key={label} className="p-5">
                  <Icon className="size-5 text-muted" aria-hidden />
                  <p className="mt-2 text-3xl font-bold">{value}</p>
                  <p className="text-sm text-muted">{label}</p>
                </CreamCard>
              ))}
            </div>
            <CreamCard className="mt-6 flex flex-col gap-4 p-6 sm:flex-row sm:items-center">
              <div className="flex-1">
                <p className="font-semibold">Demo listings near MUJ</p>
                <p className="text-sm text-muted">Adds 4 sample parking spots around Manipal University Jaipur (Dehmi Kalan, Ajmer Road, Bagru). Pressing it again only refreshes them.</p>
              </div>
              <Button arrow loading={busy} onClick={() => run("4 MUJ demo spots are now live", store.adminAddMujDemoSpots)}>Add MUJ demo spots</Button>
            </CreamCard>
          </div>
        )}

        {tab === 1 && (spots.length === 0 ? <EmptyState title="No listings yet" message="Add the MUJ demo spots from Overview." /> : (
          <div className="grid gap-4 md:grid-cols-2">
            {spots.map((s) => (
              <CreamCard key={s.id} className="p-4 animate-fade-up">
                <div className="flex gap-4">
                  <Link href={spotDetailHref(s.id)} className="shrink-0"><ParkingImage path={s.imagePaths[0]} alt={s.parkingNumber} className="size-20 rounded-lg" /></Link>
                  <div className="min-w-0 flex-1">
                    <p className="flex items-center gap-1.5 text-lg font-medium">{s.parkingNumber} {s.isVerified ? <VerifiedBadge className="size-4" /> : null}</p>
                    <p className="truncate text-sm">{s.societyName}, {s.city}</p>
                    <p className="truncate text-xs text-muted">Owner: {s.ownerName} · {priceText(s)}</p>
                    {!s.isVerified ? <p className="text-xs font-medium text-rejected">Awaiting verification</p> : null}
                  </div>
                </div>
                <div className="mt-3 flex justify-end gap-2">
                  <Button size="sm" variant="danger" onClick={() => setToRemove(s)}>Remove</Button>
                  <Button size="sm" variant={s.isVerified ? "outline" : "yellow"} disabled={busy}
                    onClick={() => run(s.isVerified ? "Verification removed" : "Listing verified", () => store.adminSetSpotVerified(s.id, !s.isVerified))}>
                    {s.isVerified ? "Un-verify" : "Verify"}
                  </Button>
                </div>
              </CreamCard>
            ))}
          </div>
        ))}

        {tab === 2 && (bookings.length === 0 ? <EmptyState title="No bookings yet" /> : (
          <div className="grid gap-4 md:grid-cols-2">
            {bookings.map((b) => (
              <BookingCard key={b.id} booking={b} verified={!!data.parkingSpots.find((s) => s.id === b.spotId)?.isVerified}
                actions={isOngoing(b) ? <Button size="sm" variant="danger" disabled={busy}
                  onClick={() => run("Booking cancelled", () => store.adminCancelBooking(b.id))}>Cancel booking</Button> : null} />
            ))}
          </div>
        ))}

        {tab === 3 && (
          <ul className="space-y-3">
            {data.users.map((u) => (
              <li key={u.id}>
                <CreamCard className="flex items-center gap-3 p-3">
                  {u.profileImagePath
                    // eslint-disable-next-line @next/next/no-img-element
                    ? <img src={u.profileImagePath} alt="" className="size-11 rounded-full object-cover" referrerPolicy="no-referrer" />
                    : <span className="grid size-11 place-items-center rounded-full bg-yellow font-bold">{u.name[0]?.toUpperCase()}</span>}
                  <div className="min-w-0 flex-1">
                    <p className="truncate font-semibold">{u.name}</p>
                    <p className="truncate text-xs text-muted">{u.email}</p>
                    <p className={u.isBlocked ? "text-xs font-bold text-rejected" : "text-xs font-bold"}>
                      {u.role === "admin" ? "Admin" : u.isBlocked ? "Blocked" : "User"}
                    </p>
                  </div>
                  {u.role !== "admin" ? (
                    <Button size="sm" variant={u.isBlocked ? "yellow" : "danger"} disabled={busy}
                      onClick={() => run(u.isBlocked ? `${u.name} unblocked` : `${u.name} blocked`, () => store.adminSetUserBlocked(u.id, !u.isBlocked))}>
                      {u.isBlocked ? "Unblock" : "Block"}
                    </Button>
                  ) : null}
                </CreamCard>
              </li>
            ))}
          </ul>
        )}
      </div>

      <ConfirmDialog open={!!toRemove} danger title={`Remove ${toRemove?.parkingNumber ?? ""}?`} confirmText="Remove"
        message="The listing is deleted and its upcoming bookings are cancelled. The renters are notified."
        onCancel={() => setToRemove(null)}
        onConfirm={() => { const id = toRemove!.id; setToRemove(null); run("Listing removed", () => store.adminDeleteSpot(id)); }} />
    </>
  );
}

export default function AdminPage() {
  return (
    <>
      <PageHeader title="Admin Portal" subtitle="Manage Parkora listings, bookings and users" back="/profile/" />
      <RequireLogin><AdminPortal /></RequireLogin>
    </>
  );
}
