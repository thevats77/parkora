"use client";

import { Search, SearchX, SlidersHorizontal, UserRound } from "lucide-react";
import { useMemo, useState } from "react";
import { PageHeader } from "@/components/layout";
import { ParkingCard } from "@/components/parking";
import { Button, CardListSkeleton, Chip, EmptyState } from "@/components/ui";
import { useParkora } from "@/lib/store";
import { cn } from "@/lib/utils";

/*
 * PARKING DISCOVERY ( /parking )
 * Web version of the Home + "View All" screens: search, filters and sorting
 * over the available spots (ParkoraStore.latestSpots()).
 */
const VEHICLES = ["SUV", "Sedan", "Regular", "Two Wheeler"];
const TYPES = ["Any", "Open", "Covered", "Basement"];

export default function DiscoverPage() {
  const { ready, latestSpots, currentUser } = useParkora();
  const [query, setQuery] = useState("");
  const [vehicle, setVehicle] = useState<string | null>(null);
  const [type, setType] = useState("Any");
  const [maxHourly, setMaxHourly] = useState("");
  const [sort, setSort] = useState<"newest" | "cheapest">("newest");
  const [showFilters, setShowFilters] = useState(false);

  const all = ready ? latestSpots() : [];
  const spots = useMemo(() => {
    const q = query.trim().toLowerCase();
    const max = Number(maxHourly) || Infinity;
    const list = all.filter((s) =>
      (!q || [s.parkingNumber, s.societyName, s.address, s.city].some((t) => t.toLowerCase().includes(q))) &&
      (!vehicle || s.vehicleTypes.includes(vehicle)) &&
      (type === "Any" || s.parkingType === type) &&
      s.pricePerHour <= max
    );
    return sort === "cheapest" ? [...list].sort((a, b) => a.pricePerHour - b.pricePerHour) : list;
  }, [all, query, vehicle, type, maxHourly, sort]);

  const activeFilters = [vehicle, type !== "Any", maxHourly].filter(Boolean).length;
  const clearFilters = () => { setQuery(""); setVehicle(null); setType("Any"); setMaxHourly(""); };

  return (
    <>
      <PageHeader title={currentUser ? `Hey, ${currentUser.name.split(" ")[0]}` : "Find Parking"}
        subtitle={currentUser ? `📍 ${currentUser.location}` : "Discover parking spaces listed by owners near you"}>
        <div className="mx-auto max-w-2xl pb-8 pt-4">
          <label className="flex h-14 items-center gap-3 rounded-2xl bg-white px-4 shadow-lg focus-within:ring-4 focus-within:ring-navy/15">
            <Search className="size-5 text-muted" aria-hidden />
            <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search parking number, society, area or city"
              aria-label="Search parking" className="h-full flex-1 bg-transparent text-[15px] outline-none placeholder:text-muted" />
          </label>
        </div>
      </PageHeader>

      <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6">
        {/* Filters – always visible on large screens, behind a "Filters" button on phones */}
        <button onClick={() => setShowFilters(!showFilters)} aria-expanded={showFilters}
          className="flex h-11 w-full cursor-pointer items-center justify-center gap-2 rounded-xl border border-card-border bg-cream font-semibold lg:hidden">
          <SlidersHorizontal className="size-4" aria-hidden /> Filters{activeFilters ? ` (${activeFilters})` : ""}
        </button>
        <div className={cn("mt-3 flex-col gap-4 rounded-2xl border border-card-border bg-cream/60 p-4 lg:mt-0 lg:flex lg:flex-row lg:items-end",
          showFilters ? "flex animate-fade-up" : "hidden")}>
          <div className="flex-1">
            <p className="mb-2 text-[13px] text-muted">Vehicle</p>
            <div className="flex flex-wrap gap-2">
              {VEHICLES.map((v) => <Chip key={v} selected={vehicle === v} onClick={() => setVehicle(vehicle === v ? null : v)}>{v}</Chip>)}
            </div>
          </div>
          <div className="grid grid-cols-3 gap-3 lg:flex lg:items-end">
            <Select label="Parking type" value={type} onChange={setType} options={TYPES.map((t) => [t, t])} />
            <label className="block">
              <span className="mb-2 block text-[13px] text-muted">Max ₹/hour</span>
              <input value={maxHourly} onChange={(e) => setMaxHourly(e.target.value.replace(/\D/g, ""))} inputMode="numeric" placeholder="Any"
                className="h-10 w-full rounded-xl border border-field-border bg-white px-3 text-sm lg:w-28" />
            </label>
            <Select label="Sort by" value={sort} onChange={(v) => setSort(v as "newest" | "cheapest")}
              options={[["newest", "Latest listed"], ["cheapest", "Lowest price"]]} />
          </div>
        </div>

        <div className="mb-5 mt-8 flex items-center justify-between">
          <h2 className="text-2xl font-medium">Latest Listed</h2>
          {ready ? <p className="text-sm text-muted">{spots.length} of {all.length} spots</p> : null}
        </div>

        {!ready ? <CardListSkeleton count={6} /> : spots.length === 0 ? (
          <EmptyState icon={all.length ? SearchX : UserRound}
            title={all.length ? "No spots match your filters" : "No parking spots listed yet"}
            message={all.length ? "Try another area or remove a filter." : "Be the first to list a parking spot."}
            action={all.length ? <Button variant="outline" onClick={clearFilters}>Clear filters</Button> : null} />
        ) : (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {spots.map((s, i) => <ParkingCard key={s.id} spot={s} index={i} />)}
          </div>
        )}
      </div>
    </>
  );
}

function Select({ label, value, onChange, options }: { label: string; value: string; onChange: (v: string) => void; options: [string, string][] }) {
  return (
    <label className="block">
      <span className="mb-2 block text-[13px] text-muted">{label}</span>
      <select value={value} onChange={(e) => onChange(e.target.value)} className="h-10 w-full rounded-xl border border-field-border bg-white px-2 text-sm">
        {options.map(([v, text]) => <option key={v} value={v}>{text}</option>)}
      </select>
    </label>
  );
}
