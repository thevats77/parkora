"use client";

import { BadgeCheck, CircleArrowRight, CircleCheck, CircleX, Clock, Eye, EyeOff, type LucideIcon } from "lucide-react";
import Link from "next/link";
import { useId, useState } from "react";
import { statusLabel, type BookingStatus } from "@/lib/types";
import { cn } from "@/lib/utils";

/*
 * SMALL REUSABLE UI PIECES – the web versions of CommonComponents.kt
 * (YellowButton, ParkoraTextField, StatusBadge, VerifiedBadge, CreamCard, EmptyState, chips).
 */

// ---------------- Buttons ----------------
type ButtonVariant = "yellow" | "outline" | "danger" | "ghost";

const variantClasses: Record<ButtonVariant, string> = {
  yellow: "btn-yellow text-navy shadow-[0_2px_6px_rgb(0_0_0/0.12)] hover:brightness-[1.03] active:brightness-95",
  outline: "border border-navy bg-white text-navy hover:bg-cream",
  danger: "border border-rejected bg-white text-rejected hover:bg-red-50",
  ghost: "text-navy hover:bg-black/5",
};

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: "sm" | "md" | "lg";
  arrow?: boolean;   // show the ➜ circle icon like the app's buttons
  loading?: boolean;
}

export function Button({ variant = "yellow", size = "md", arrow = false, loading = false, className, children, disabled, ...rest }: ButtonProps) {
  const sizes = { sm: "h-9 px-3.5 text-[13px] rounded-lg", md: "h-11 px-5 text-[15px] rounded-xl", lg: "h-[54px] px-6 text-base rounded-xl" };
  return (
    <button
      {...rest}
      disabled={disabled || loading}
      className={cn(
        "inline-flex items-center justify-center gap-2 font-bold transition duration-150 select-none cursor-pointer",
        "disabled:cursor-not-allowed disabled:opacity-50",
        variantClasses[variant], sizes[size], className
      )}
    >
      {loading ? <span className="size-4 animate-spin rounded-full border-2 border-navy/30 border-t-navy" aria-hidden /> : null}
      {children}
      {arrow && !loading ? <CircleArrowRight className={size === "sm" ? "size-4" : "size-[18px]"} fill="currentColor" stroke="var(--color-btn-top)" aria-hidden /> : null}
    </button>
  );
}

/** A Link that looks like a Button. */
export function ButtonLink({ href, variant = "yellow", size = "md", arrow = false, className, children }: {
  href: string; variant?: ButtonVariant; size?: "sm" | "md" | "lg"; arrow?: boolean; className?: string; children: React.ReactNode;
}) {
  const sizes = { sm: "h-9 px-3.5 text-[13px] rounded-lg", md: "h-11 px-5 text-[15px] rounded-xl", lg: "h-[54px] px-6 text-base rounded-xl" };
  return (
    <Link href={href} className={cn("inline-flex items-center justify-center gap-2 font-bold transition duration-150", variantClasses[variant], sizes[size], className)}>
      {children}
      {arrow ? <CircleArrowRight className="size-[18px]" fill="currentColor" stroke="var(--color-btn-top)" aria-hidden /> : null}
    </Link>
  );
}

// ---------------- Form fields ----------------
interface TextFieldProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, "onChange"> {
  label: string;
  icon?: LucideIcon;
  error?: string | null;
  value: string;
  onChange: (value: string) => void;
  hint?: string;
}

/** Label above, then a cream box: icon │ text – exactly like ParkoraTextField in the app. */
export function TextField({ label, icon: Icon, error, value, onChange, type = "text", hint, className, ...rest }: TextFieldProps) {
  const id = useId();
  const [showPassword, setShowPassword] = useState(false);
  const isPassword = type === "password";
  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1.5 ml-1.5 block text-[13px] text-muted">{label}</label>
      <div className={cn(
        "flex h-[54px] items-center rounded-[10px] border bg-cream px-3.5 transition focus-within:border-navy/40 focus-within:shadow-[0_0_0_3px_rgb(254_215_0/0.35)]",
        error ? "border-rejected" : "border-field-border"
      )}>
        {Icon ? (
          <>
            <Icon className="size-[21px] shrink-0 text-navy" aria-hidden />
            <span className="mx-3 h-[26px] w-px bg-[#e6e1c8]" aria-hidden />
          </>
        ) : null}
        <input
          id={id}
          type={isPassword && showPassword ? "text" : type}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          aria-invalid={!!error}
          aria-describedby={error ? `${id}-error` : undefined}
          className="h-full min-w-0 flex-1 bg-transparent text-[15px] text-navy outline-none placeholder:text-muted/80"
          {...rest}
        />
        {isPassword ? (
          <button type="button" onClick={() => setShowPassword(!showPassword)} className="ml-2 rounded p-1 text-muted hover:text-navy"
            aria-label={showPassword ? "Hide password" : "Show password"}>
            {showPassword ? <EyeOff className="size-5" /> : <Eye className="size-5" />}
          </button>
        ) : null}
      </div>
      {error ? <p id={`${id}-error`} role="alert" className="ml-1.5 mt-1 text-xs text-rejected">{error}</p>
        : hint ? <p className="ml-1.5 mt-1 text-xs text-muted">{hint}</p> : null}
    </div>
  );
}

export function FieldLabel({ children }: { children: React.ReactNode }) {
  return <p className="mb-1.5 ml-1.5 mt-4 text-[13px] text-muted">{children}</p>;
}

export function ErrorText({ error }: { error?: string | null }) {
  return error ? <p role="alert" className="ml-1.5 mt-1 text-xs text-rejected">{error}</p> : null;
}

/** Selectable pill, e.g. vehicle types in the listing form. */
export function Chip({ selected, onClick, children }: { selected: boolean; onClick: () => void; children: React.ReactNode }) {
  return (
    <button
      type="button"
      aria-pressed={selected}
      onClick={onClick}
      className={cn(
        "rounded-full border px-4 py-2 text-sm transition cursor-pointer",
        selected ? "border-btn-bottom bg-yellow font-semibold text-navy" : "border-[#e8e2c0] bg-cream text-navy hover:border-btn-bottom"
      )}
    >
      {children}
    </button>
  );
}

// ---------------- Cards & badges ----------------
export function CreamCard({ className, children, as = "div" }: { className?: string; children: React.ReactNode; as?: "div" | "article" | "section" }) {
  const Tag = as;
  return (
    <Tag className={cn("rounded-2xl border border-card-border bg-cream shadow-[0_2px_10px_rgb(254_215_0/0.18)]", className)}>
      {children}
    </Tag>
  );
}

export function VerifiedBadge({ className }: { className?: string }) {
  return <BadgeCheck className={cn("size-5 shrink-0", className)} fill="var(--color-verified)" stroke="white" aria-label="Verified" />;
}

const statusStyle: Record<BookingStatus, { bg: string; icon: LucideIcon }> = {
  PENDING: { bg: "bg-pending", icon: Clock },
  APPROVED: { bg: "bg-approved", icon: CircleCheck },
  COMPLETED: { bg: "bg-approved", icon: CircleCheck },
  REJECTED: { bg: "bg-rejected", icon: CircleX },
  CANCELLED: { bg: "bg-rejected", icon: CircleX },
  EXPIRED: { bg: "bg-rejected", icon: CircleX },
};

export function StatusBadge({ status }: { status: BookingStatus }) {
  const { bg, icon: Icon } = statusStyle[status];
  return (
    <span className={cn("inline-flex items-center gap-1 rounded-md px-2 py-1 text-xs font-medium text-white", bg)}>
      <Icon className="size-3.5" aria-hidden /> {statusLabel[status]}
    </span>
  );
}

// ---------------- Empty, loading ----------------
export function EmptyState({ icon: Icon, title, message, action }: { icon?: LucideIcon; title: string; message?: string; action?: React.ReactNode }) {
  return (
    <div className="mx-auto flex max-w-sm flex-col items-center px-6 py-16 text-center animate-fade-up">
      {Icon ? (
        <div className="mb-4 grid size-16 place-items-center rounded-full bg-yellow/30">
          <Icon className="size-7 text-navy" aria-hidden />
        </div>
      ) : null}
      <p className="text-lg font-semibold">{title}</p>
      {message ? <p className="mt-1 text-sm text-muted">{message}</p> : null}
      {action ? <div className="mt-5">{action}</div> : null}
    </div>
  );
}

export function Skeleton({ className }: { className?: string }) {
  return <div className={cn("animate-pulse rounded-xl bg-[#f3f0dc]", className)} aria-hidden />;
}

/** Loading placeholder shaped like a list of cards. */
export function CardListSkeleton({ count = 3 }: { count?: number }) {
  return (
    <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3" aria-busy="true" aria-label="Loading">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="overflow-hidden rounded-2xl border border-card-border bg-cream">
          <Skeleton className="h-44 rounded-none" />
          <div className="space-y-3 p-5"><Skeleton className="h-5 w-24" /><Skeleton className="h-4 w-3/4" /><Skeleton className="h-4 w-1/2" /></div>
        </div>
      ))}
    </div>
  );
}
