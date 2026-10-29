"use client";

import { ArrowRight, Bell, ShieldUser, CalendarDays, Camera, CarFront, ClipboardList, LogOut, ShieldCheck, UserCog, type LucideIcon } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useRef, useState } from "react";
import { Avatar, ConfirmDialog, PageHeader, RequireLogin, useToast } from "@/components/layout";
import { resizeImage } from "@/lib/image";
import { useParkora } from "@/lib/store";

/*
 * PROFILE ( /profile ) – Figure 2
 * Avatar with camera button, "Name | email | phone" and the menu from the app.
 */
function Profile() {
  const { currentUser, isAdmin, mySpots, bookingRequestsForMe, unreadCount, updateProfilePhoto, logout } = useParkora();
  const router = useRouter();
  const toast = useToast();
  const fileInput = useRef<HTMLInputElement>(null);
  const [confirmLogout, setConfirmLogout] = useState(false);
  const user = currentUser!;
  const pending = bookingRequestsForMe().filter((b) => b.status === "PENDING").length;

  async function changePhoto(file?: File) {
    if (!file) return;
    try {
      const err = await updateProfilePhoto(await resizeImage(file, 400, 0.8));
      toast(err ?? "Profile photo updated");
    } catch (e) {
      toast((e as Error).message);
    }
  }

  const items: [LucideIcon, string, string, string?][] = [
    ...(isAdmin ? [[ShieldUser, "Admin Portal", "/admin/", "Admin"] as [LucideIcon, string, string, string]] : []),
    [CarFront, "My Parking Spots", "/my-spots/", mySpots().length ? String(mySpots().length) : undefined],
    [CalendarDays, "My Bookings", "/bookings/"],
    [ClipboardList, "Booking Requests", "/requests/", pending ? `${pending} new` : undefined],
    [Bell, "Notifications", "/notifications/", unreadCount() ? `${unreadCount()} new` : undefined],
    [UserCog, "Edit Profile", "/profile/edit/"],
    [ShieldCheck, "Privacy Policy", "/privacy/"],
  ];

  return (
    <div className="mx-auto max-w-xl px-4 py-8">
      <div className="flex flex-col items-center text-center animate-fade-up">
        <div className="relative">
          <Avatar name={user.name} photo={user.profileImagePath} />
          <button onClick={() => fileInput.current?.click()} aria-label="Change profile photo"
            className="absolute bottom-0 right-0 grid size-9 place-items-center rounded-lg bg-white shadow"><Camera className="size-5" /></button>
          <input ref={fileInput} type="file" accept="image/*" hidden onChange={(e) => { changePhoto(e.target.files?.[0]); e.target.value = ""; }} />
        </div>
        <p className="mt-4 leading-7">{user.name} |<br />{user.email} |<br />{user.phone || "Add your phone in Edit Profile"}</p>
      </div>

      <nav className="mt-8 space-y-3.5" aria-label="Profile menu">
        {items.map(([Icon, title, href, badge], i) => (
          <Link key={href} href={href} style={{ animationDelay: `${i * 40}ms` }}
            className="flex h-[58px] items-center rounded-lg border border-card-border bg-cream px-4 shadow-[0_2px_8px_rgb(254_215_0/0.18)] transition hover:-translate-y-0.5 hover:border-btn-bottom animate-fade-up">
            <MenuInner Icon={Icon} title={title} badge={badge} />
          </Link>
        ))}
        <button onClick={() => setConfirmLogout(true)}
          className="flex h-[58px] w-full cursor-pointer items-center rounded-lg border border-card-border bg-cream px-4 text-left shadow-[0_2px_8px_rgb(254_215_0/0.18)] transition hover:-translate-y-0.5 hover:border-btn-bottom">
          <MenuInner Icon={LogOut} title="Logout" />
        </button>
      </nav>

      <ConfirmDialog open={confirmLogout} title="Logout" message="Are you sure you want to log out?" confirmText="Logout"
        onCancel={() => setConfirmLogout(false)} onConfirm={async () => { setConfirmLogout(false); await logout(); router.push("/login/"); }} />
    </div>
  );
}

function MenuInner({ Icon, title, badge }: { Icon: LucideIcon; title: string; badge?: string }) {
  return (
    <>
      <Icon className="size-6 shrink-0" aria-hidden />
      <span className="mx-3.5 h-[30px] w-[1.5px] bg-navy" aria-hidden />
      <span className="flex-1 text-[17px] font-medium">{title}</span>
      {badge ? <span className="mr-3 rounded-full bg-yellow px-2 py-0.5 text-xs font-bold">{badge}</span> : null}
      <ArrowRight className="size-5" aria-hidden />
    </>
  );
}

export default function ProfilePage() {
  return (
    <>
      <PageHeader title="Profile" />
      <RequireLogin><Profile /></RequireLogin>
    </>
  );
}
