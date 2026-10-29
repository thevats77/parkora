/** Yellow header with the Parkora logo + white form card (Login & Register). */
export function AuthShell({ subtitle, children }: { subtitle: string; children: React.ReactNode }) {
  return (
    <div>
      <div className="yellow-circles rounded-b-[40px] px-4 pb-24 pt-10 text-center">
        <span className="mx-auto grid size-20 place-items-center rounded-full bg-navy text-4xl font-bold text-yellow">P</span>
        <p className="mt-3 text-3xl font-bold tracking-[0.18em]">PARKORA</p>
        <p className="text-sm">{subtitle}</p>
      </div>
      <div className="mx-auto -mt-16 max-w-md px-4">
        <div className="rounded-3xl border border-card-border bg-white p-6 shadow-xl sm:p-8 animate-fade-up">{children}</div>
      </div>
    </div>
  );
}
