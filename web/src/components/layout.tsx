"use client";

import { ArrowLeft, Bell, Home, LogIn, Plus, User } from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { createContext, useCallback, useContext, useEffect, useRef, useState } from "react";
import { useParkora } from "@/lib/store";
import { cn, initials } from "@/lib/utils";
import { Button } from "./ui";

/*
 * PAGE LAYOUT PIECES
 *  - TopNav:      website navigation bar (desktop) / slim bar with logo + bell (mobile)
 *  - MobileBar:   Home | + | Profile bar at the bottom on phones (same as the Android app)
 *  - PageHeader:  yellow header with rounded bottom corners (YellowHeader in the app)
 *  - Tabs, StepIndicator, ConfirmDialog, Toasts, RequireLogin
 */

export function Logo({ compact = false }: { compact?: boolean }) {
  return (
    <Link href="/" className="flex items-center gap-2" aria-label="Parkora home">
      <span className="grid size-9 place-items-center rounded-xl bg-navy text-lg font-bold text-yellow">P</span>
      {!compact ? <span className="text-lg font-bold tracking-[0.18em]">PARKORA</span> : null}
    </Link>
  );
}

const navLinks = [
  { href: "/parking/", label: "Find Parking" },
  { href: "/bookings/", label: "My Bookings" },
  { href: "/my-spots/", label: "My Spots" },
  { href: "/requests/", label: "Requests" },
];

export function TopNav() {
  const { currentUser, unreadCount, ready, isAdmin } = useParkora();
  const pathname = usePathname();
  const unread = ready && currentUser ? unreadCount() : 0;

  return (
    <header className="sticky top-0 z-40 border-b border-card-border bg-white/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center gap-6 px-4 sm:px-6">
        <Logo />
        <nav className="hidden flex-1 items-center gap-1 md:flex" aria-label="Main">
          {[...navLinks, ...(isAdmin ? [{ href: "/admin/", label: "Admin" }] : [])].map((l) => (
            <Link key={l.href} href={l.href}
              className={cn("rounded-lg px-3 py-2 text-sm font-medium transition hover:bg-cream",
                pathname.startsWith(l.href) ? "bg-yellow/25 text-navy" : "text-navy/80")}>
              {l.label}
            </Link>
          ))}
        </nav>
        <div className="ml-auto flex items-center gap-2">
          {currentUser ? (
            <>
              <Link href="/list-spot/" className="hidden h-10 items-center gap-1.5 rounded-xl px-4 text-sm font-bold btn-yellow md:inline-flex">
                <Plus className="size-4" aria-hidden /> List a Spot
              </Link>
              <Link href="/notifications/" className="relative rounded-full p-2 hover:bg-cream" aria-label={`Notifications${unread ? `, ${unread} unread` : ""}`}>
                <Bell className="size-6" fill="currentColor" aria-hidden />
                {unread ? <span className="absolute right-1.5 top-1.5 size-2.5 rounded-full bg-rejected ring-2 ring-white" /> : null}
              </Link>
              <Link href="/profile/" className="hidden md:block" aria-label="Profile">
                <Avatar name={currentUser.name} photo={currentUser.profileImagePath} size="sm" />
              </Link>
            </>
          ) : ready ? (
            <Link href="/login/" className="inline-flex h-10 items-center gap-1.5 rounded-xl px-4 text-sm font-bold btn-yellow">
              <LogIn className="size-4" aria-hidden /> Log in
            </Link>
          ) : null}
        </div>
      </div>
    </header>
  );
}

/** Bottom bar for phones – Home, big yellow +, Profile (like the Android bottom bar). */
export function MobileBar() {
  const pathname = usePathname();
  const { currentUser } = useParkora();
  const item = (href: string, label: string, Icon: typeof Home, active: boolean) => (
    <Link href={href} className="flex flex-1 flex-col items-center justify-center gap-0.5 py-2" aria-current={active ? "page" : undefined}>
      <Icon className="size-7" fill={active ? "currentColor" : "none"} aria-hidden />
      <span className={cn("text-xs", active ? "font-bold" : "text-muted")}>{label}</span>
    </Link>
  );
  return (
    <nav className="fixed inset-x-0 bottom-0 z-40 md:hidden" aria-label="Bottom">
      <div className="relative rounded-t-[28px] bg-cream shadow-[0_-6px_20px_rgb(0_0_0/0.08)] pb-[env(safe-area-inset-bottom)]">
        <div className="flex h-16 items-center">
          {item("/parking/", "Home", Home, pathname.startsWith("/parking") || pathname === "/")}
          <div className="flex-[0.8]" />
          {item(currentUser ? "/profile/" : "/login/", "Profile", User, pathname.startsWith("/profile"))}
        </div>
        <Link href="/list-spot/" aria-label="List a parking spot"
          className="absolute left-1/2 top-0 grid size-[70px] -translate-x-1/2 -translate-y-1/2 place-items-center rounded-full bg-white p-1.5 shadow-lg">
          <span className="grid size-full place-items-center rounded-full bg-yellow transition active:scale-95"><Plus className="size-7" aria-hidden /></span>
        </Link>
      </div>
    </nav>
  );
}

export function Avatar({ name, photo, size = "lg" }: { name: string; photo: string | null; size?: "sm" | "lg" }) {
  const box = size === "sm" ? "size-9 text-sm" : "size-28 text-4xl";
  return photo ? (
    // eslint-disable-next-line @next/next/no-img-element
    <img src={photo} alt={name} className={cn(box, "rounded-full border border-navy/50 object-cover")} />
  ) : (
    <span className={cn(box, "grid place-items-center rounded-full border border-navy/50 bg-yellow font-bold")}>{initials(name)}</span>
  );
}

/** Yellow page header with rounded bottom corners, optional back button and tabs. */
export function PageHeader({ title, subtitle, back, children }: { title: string; subtitle?: string; back?: string | boolean; children?: React.ReactNode }) {
  const router = useRouter();
  return (
    <div className="yellow-circles rounded-b-[36px]">
      <div className="mx-auto max-w-6xl px-4 pt-6 sm:px-6">
        <div className="relative flex min-h-12 items-center justify-center">
          {back ? (
            <button onClick={() => (typeof back === "string" ? router.push(back) : router.back())}
              className="absolute left-0 rounded-full p-2 hover:bg-black/5" aria-label="Go back">
              <ArrowLeft className="size-7" aria-hidden />
            </button>
          ) : null}
          <div className="text-center">
            <h1 className="text-2xl font-medium sm:text-[28px]">{title}</h1>
            {subtitle ? <p className="mt-0.5 text-sm text-navy/75">{subtitle}</p> : null}
          </div>
        </div>
        {children ?? <div className="h-6" />}
      </div>
    </div>
  );
}

/** "Ongoing | Past" tabs inside the yellow header. */
export function Tabs({ tabs, selected, onSelect }: { tabs: string[]; selected: number; onSelect: (i: number) => void }) {
  return (
    <div role="tablist" className="mx-auto mt-4 flex max-w-xl">
      {tabs.map((t, i) => (
        <button key={t} role="tab" aria-selected={i === selected} onClick={() => onSelect(i)}
          className={cn("flex-1 cursor-pointer pb-0 pt-1 text-lg transition", i === selected ? "font-semibold" : "text-muted hover:text-navy")}>
          <span className="block pb-2">{t}</span>
          <span className={cn("block h-1 rounded-full transition", i === selected ? "bg-navy" : "bg-transparent")} />
        </button>
      ))}
    </div>
  );
}

export const listingSteps = ["Location/\nAddress", "Parking\nSize", "Upload\nDocuments", "Pricing"];

/** ①━②━③━④ indicator (Figure 3). */
export function StepIndicator({ current }: { current: number }) {
  return (
    <ol className="flex" aria-label={`Step ${current} of 4`}>
      {listingSteps.map((title, i) => {
        const step = i + 1;
        const active = step <= current;
        return (
          <li key={title} className="flex flex-1 flex-col items-center" aria-current={step === current ? "step" : undefined}>
            <div className="relative flex h-8 w-full items-center justify-center">
              {i > 0 ? <span className={cn("absolute left-0 right-1/2 h-0.5", active ? "bg-navy" : "bg-light-gray")} /> : null}
              {i < 3 ? <span className={cn("absolute left-1/2 right-0 h-0.5", step < current ? "bg-navy" : "bg-light-gray")} /> : null}
              <span className={cn("relative grid size-8 place-items-center rounded-full text-sm font-bold text-white transition", active ? "bg-navy" : "bg-light-gray")}>{step}</span>
            </div>
            <span className="mt-1.5 whitespace-pre-line text-center text-[11px] leading-tight sm:text-xs">{title}</span>
          </li>
        );
      })}
    </ol>
  );
}

/** Simple accessible confirmation dialog. */
export function ConfirmDialog({ open, title, message, confirmText, cancelText = "Cancel", danger, onConfirm, onCancel }: {
  open: boolean; title: string; message: React.ReactNode; confirmText: string; cancelText?: string; danger?: boolean; onConfirm: () => void; onCancel: () => void;
}) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    if (open && !ref.current?.open) ref.current?.showModal();
    if (!open && ref.current?.open) ref.current?.close();
  }, [open]);
  return (
    <dialog ref={ref} onCancel={(e) => { e.preventDefault(); onCancel(); }}
      className="m-auto w-[min(92vw,420px)] rounded-3xl bg-white p-0 text-navy shadow-2xl backdrop:bg-black/40 backdrop:backdrop-blur-[2px] open:animate-pop">
      <div className="p-6">
        <h2 className="text-xl font-semibold">{title}</h2>
        <div className="mt-2 text-[15px] text-muted">{message}</div>
        <div className="mt-6 flex justify-end gap-2">
          <Button variant="ghost" onClick={onCancel}>{cancelText}</Button>
          <Button variant={danger ? "danger" : "yellow"} onClick={onConfirm} autoFocus>{confirmText}</Button>
        </div>
      </div>
    </dialog>
  );
}

// ---------------- Toast messages (like Android's Toast) ----------------
const ToastContext = createContext<(message: string) => void>(() => {});

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<{ id: number; message: string }[]>([]);
  const show = useCallback((message: string) => {
    const id = Date.now() + Math.random();
    setToasts((t) => [...t, { id, message }]);
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 3200);
  }, []);
  return (
    <ToastContext.Provider value={show}>
      {children}
      <div className="pointer-events-none fixed inset-x-0 bottom-24 z-50 flex flex-col items-center gap-2 px-4 md:bottom-8" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className="animate-pop rounded-full bg-navy px-5 py-3 text-sm font-medium text-white shadow-xl">{t.message}</div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export const useToast = () => useContext(ToastContext);

/**
 * Wrap pages that need a logged-in user. While data loads it shows `loading`;
 * if nobody is logged in it shows a friendly message with a Log in button.
 */
export function RequireLogin({ children, loading }: { children: React.ReactNode; loading?: React.ReactNode }) {
  const { ready, currentUser } = useParkora();
  const pathname = usePathname();
  if (!ready) return <>{loading ?? <div className="h-64" aria-busy="true" />}</>;
  if (!currentUser) {
    return (
      <div className="mx-auto max-w-md px-6 py-20 text-center animate-fade-up">
        <p className="text-xl font-semibold">Please sign in</p>
        <p className="mt-1 text-muted">Sign in with your Google account to see this page.</p>
        <Link href={`/login/?next=${encodeURIComponent(pathname)}`} className="mt-6 inline-flex h-11 items-center rounded-xl px-6 font-bold btn-yellow">Sign in with Google</Link>
      </div>
    );
  }
  return <>{children}</>;
}
