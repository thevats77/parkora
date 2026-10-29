/*
 * BOOKING PRICE CALCULATION – same rule as the Android app (BookingCalculator.kt)
 *   - hours are rounded UP (a started hour counts as a full hour)
 *   - less than 24 hours  -> charge per HOUR
 *   - 24 hours or more    -> charge per DAY (a started day counts as a full day)
 * Example: 22-09 7:00 AM -> 30-09 11:00 PM = 208 h = 8.67 days -> 9 days.
 */

export interface PriceSummary {
  totalHours: number;
  billedUnits: number;
  isDaily: boolean;
  unitPrice: number;
  totalPrice: number;
  unitName: string;   // "Hour(s)" | "Day(s)"
  shortUnit: string;  // "hr" | "day"
}

const ONE_HOUR = 60 * 60 * 1000;

export function calculatePrice(
  checkIn: number,
  checkOut: number,
  pricePerHour: number,
  pricePerDay: number
): PriceSummary | null {
  const duration = checkOut - checkIn;
  if (duration <= 0) return null;

  const totalHours = Math.ceil(duration / ONE_HOUR);

  if (totalHours < 24) {
    return {
      totalHours,
      billedUnits: totalHours,
      isDaily: false,
      unitPrice: pricePerHour,
      totalPrice: totalHours * pricePerHour,
      unitName: "Hour(s)",
      shortUnit: "hr",
    };
  }
  const totalDays = Math.ceil(totalHours / 24);
  return {
    totalHours,
    billedUnits: totalDays,
    isDaily: true,
    unitPrice: pricePerDay,
    totalPrice: totalDays * pricePerDay,
    unitName: "Day(s)",
    shortUnit: "day",
  };
}

/** 208 -> "8 day(s) 16 hour(s)" */
export function durationText(totalHours: number): string {
  const days = Math.floor(totalHours / 24);
  const hours = totalHours % 24;
  if (days === 0) return `${hours} hour(s)`;
  if (hours === 0) return `${days} day(s)`;
  return `${days} day(s) ${hours} hour(s)`;
}
