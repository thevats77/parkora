"use client";

import { CarFront, Trash2 } from "lucide-react";
import Link from "next/link";
import { useState } from "react";
import { ConfirmDialog, PageHeader, RequireLogin, useToast } from "@/components/layout";
import { ParkingImage, priceText, spotDetailHref } from "@/components/parking";
import { ButtonLink, CardListSkeleton, CreamCard, EmptyState, VerifiedBadge } from "@/components/ui";
import { useParkora } from "@/lib/store";
import type { ParkingSpot } from "@/lib/types";

/*
 * MY PARKING SPOTS ( /my-spots ) – owner dashboard
 * Pause/resume bookings for a spot, delete a spot (blocked while it has active bookings),
 * or list a new one.
 */
function MySpots() {
  const { mySpots, setSpotAvailability, deleteSpot } = useParkora();
  const toast = useToast();
  const [toDelete, setToDelete] = useState<ParkingSpot | null>(null);
  const spots = mySpots();

  if (spots.length === 0) {
    return <EmptyState icon={CarFront} title="No parking spots yet" message="List your first spot in four quick steps."
      action={<ButtonLink href="/list-spot/" arrow>List a Parking Spot</ButtonLink>} />;
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6">
      <div className="mb-5 flex items-center justify-between">
        <p className="text-sm text-muted">{spots.length} spot{spots.length > 1 ? "s" : ""} listed</p>
        <ButtonLink href="/list-spot/" size="sm" arrow>List a New Spot</ButtonLink>
      </div>
      <div className="grid gap-5 md:grid-cols-2">
        {spots.map((spot) => (
          <CreamCard key={spot.id} as="article" className="flex gap-4 p-4 animate-fade-up">
            <Link href={spotDetailHref(spot.id)} className="shrink-0">
              <ParkingImage path={spot.imagePaths[0]} alt={`Parking ${spot.parkingNumber}`} className="size-24 rounded-lg" />
            </Link>
            <div className="min-w-0 flex-1">
              <p className="flex items-center gap-1.5 text-lg font-medium">{spot.parkingNumber} {spot.isVerified ? <VerifiedBadge className="size-4" /> : null}</p>
              <p className="truncate text-sm">{spot.societyName}</p>
              <p className="text-sm font-medium">{priceText(spot)}</p>
              {!spot.isVerified ? <p className="text-xs text-muted">Verification pending</p> : null}
              <div className="mt-2 flex items-center justify-between gap-2">
                <label className="flex cursor-pointer items-center gap-2 text-xs text-muted">
                  <input type="checkbox" role="switch" checked={spot.isAvailable}
                    onChange={async (e) => { const on = e.target.checked; toast((await setSpotAvailability(spot.id, on)) ?? (on ? "Accepting bookings" : "Bookings paused")); }}
                    className="h-6 w-10 cursor-pointer appearance-none rounded-full bg-light-gray transition before:block before:size-5 before:translate-x-0.5 before:rounded-full before:bg-white before:shadow before:transition checked:bg-yellow checked:before:translate-x-[18px] checked:before:bg-navy" />
                  {spot.isAvailable ? "Accepting bookings" : "Bookings paused"}
                </label>
                <button onClick={() => setToDelete(spot)} className="rounded-full p-2 text-rejected hover:bg-red-50" aria-label={`Delete ${spot.parkingNumber}`}>
                  <Trash2 className="size-5" />
                </button>
              </div>
            </div>
          </CreamCard>
        ))}
      </div>
      <ConfirmDialog open={!!toDelete} danger title={`Delete ${toDelete?.parkingNumber ?? ""}?`} confirmText="Delete"
        message="This removes the listing. This cannot be undone."
        onCancel={() => setToDelete(null)}
        onConfirm={async () => { const id = toDelete!.id; setToDelete(null); toast((await deleteSpot(id)) ?? "Parking spot deleted"); }} />
    </div>
  );
}

export default function MySpotsPage() {
  return (
    <>
      <PageHeader title="My Parking Spots" back />
      <RequireLogin loading={<div className="mx-auto max-w-4xl px-4 py-8"><CardListSkeleton count={2} /></div>}><MySpots /></RequireLogin>
    </>
  );
}
