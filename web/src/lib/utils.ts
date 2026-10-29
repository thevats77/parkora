/*
 * Small helpers: validation, date formatting, password hashing, ids.
 * Each validate function returns an ERROR MESSAGE, or null when the value is OK
 * (exactly like Validation.kt in the Android app).
 */

// ---------- Validation ----------
export function validateName(name: string): string | null {
  if (!name.trim()) return "Please enter your name";
  if (name.trim().length < 3) return "Name must have at least 3 characters";
  return null;
}

export function validateEmail(email: string): string | null {
  if (!email.trim()) return "Please enter your email";
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) return "Please enter a valid email address";
  return null;
}

export function validatePhone(phone: string): string | null {
  if (!phone.trim()) return "Please enter your phone number";
  if (!/^\d{10}$/.test(phone)) return "Phone number must be exactly 10 digits";
  return null;
}

export function validatePassword(password: string): string | null {
  if (!password) return "Please enter a password";
  if (password.length < 6) return "Password must have at least 6 characters";
  return null;
}

export function required(value: string, fieldName: string): string | null {
  return value.trim() ? null : `Please enter ${fieldName}`;
}

export function positiveNumber(value: string, fieldName: string): string | null {
  if (!value.trim()) return `Please enter ${fieldName}`;
  if (!/^\d+$/.test(value)) return `${fieldName} must be a whole number`;
  if (Number(value) <= 0) return `${fieldName} must be greater than 0`;
  if (Number(value) > 1_000_000) return `${fieldName} is too large`;
  return null;
}

// ---------- Dates ----------
const pad = (n: number) => String(n).padStart(2, "0");

/** Date -> "2026-10-26" (value format of <input type="date">) */
export function toDateInput(d: Date): string {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

/** Date -> "07:00" (value format of <input type="time">) */
export function toTimeInput(d: Date): string {
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** "2026-10-26" + "07:00" -> milliseconds (local time) */
export function toMillis(date: string, time: string): number {
  const [y, m, d] = date.split("-").map(Number);
  const [h, min] = time.split(":").map(Number);
  return new Date(y, m - 1, d, h, min).getTime();
}

/** Booking cards: "Sep 22, 25, 7:00 AM" (same format as the Android app) */
export function formatBookingDate(millis: number): string {
  const d = new Date(millis);
  const month = d.toLocaleString("en-US", { month: "short" });
  const time = d.toLocaleString("en-US", { hour: "numeric", minute: "2-digit", hour12: true });
  return `${month} ${pad(d.getDate())}, ${String(d.getFullYear()).slice(2)}, ${time}`;
}

export function formatShortDateTime(millis: number): string {
  return new Date(millis).toLocaleString("en-IN", {
    day: "2-digit", month: "short", hour: "numeric", minute: "2-digit", hour12: true,
  });
}

/** Next full hour from now – the default check-in time. */
export function nextFullHour(): Date {
  const d = new Date();
  d.setMinutes(0, 0, 0);
  d.setHours(d.getHours() + 1);
  return d;
}

// ---------- Misc ----------
/** SHA-256 hash of a password, as hex text (same result as PasswordUtils.kt). */
export async function hashPassword(password: string): Promise<string> {
  const bytes = new TextEncoder().encode(password);
  const digest = await crypto.subtle.digest("SHA-256", bytes);
  return Array.from(new Uint8Array(digest)).map((b) => b.toString(16).padStart(2, "0")).join("");
}

export function newId(): string {
  return crypto.randomUUID();
}

export function rupees(amount: number): string {
  return `₹${amount.toLocaleString("en-IN")}`;
}

/** "Vatsal Sharma" -> "VS" */
export function initials(name: string): string {
  return name.split(" ").filter(Boolean).slice(0, 2).map((w) => w[0].toUpperCase()).join("");
}

/** Joins CSS class names, skipping false/undefined ones. */
export function cn(...classes: (string | false | null | undefined)[]): string {
  return classes.filter(Boolean).join(" ");
}
