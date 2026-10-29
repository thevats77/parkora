"use client";

import { BellOff } from "lucide-react";
import { useEffect, useState } from "react";
import { PageHeader, RequireLogin } from "@/components/layout";
import { CreamCard, EmptyState } from "@/components/ui";
import { useParkora } from "@/lib/store";
import type { AppNotification } from "@/lib/types";
import { cn, formatShortDateTime } from "@/lib/utils";

/*
 * NOTIFICATIONS ( /notifications )
 * In-app messages created when bookings are requested, approved, rejected or cancelled.
 * They are NOT push notifications (no Firebase Cloud Messaging in this version).
 */
function Notifications() {
  const { myNotifications, markMyNotificationsRead } = useParkora();
  // Keep the list as it was when the page opened, so unread dots stay visible on this visit.
  const [list] = useState<AppNotification[]>(myNotifications);
  useEffect(() => { markMyNotificationsRead(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  if (list.length === 0) return <EmptyState icon={BellOff} title="No notifications yet" message="Booking updates will appear here." />;

  return (
    <ul className="mx-auto max-w-2xl space-y-3 px-4 py-8">
      {list.map((n, i) => (
        <li key={n.id} className="animate-fade-up" style={{ animationDelay: `${i * 40}ms` }}>
          <CreamCard className="flex gap-3 p-4">
            <span className={cn("mt-1.5 size-2.5 shrink-0 rounded-full", n.isRead ? "bg-transparent" : "bg-rejected")} aria-label={n.isRead ? undefined : "Unread"} />
            <div>
              <p className="font-semibold">{n.title}</p>
              <p className="text-sm">{n.message}</p>
              <p className="mt-1 text-xs text-muted">{formatShortDateTime(n.createdAt)}</p>
            </div>
          </CreamCard>
        </li>
      ))}
    </ul>
  );
}

export default function NotificationsPage() {
  return (
    <>
      <PageHeader title="Notifications" back />
      <RequireLogin><Notifications /></RequireLogin>
    </>
  );
}
