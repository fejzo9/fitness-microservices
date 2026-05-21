export function PlanTreninga() {
  return (
    <>
      {/* Page Title and Actions */}
      <div className="mb-6">
        <h2
          className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3"
          style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
        >
          Nedeljni plan treninga
        </h2>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-3 mb-6">
        <button className="bg-primary text-white px-5 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
          + Novi plan
        </button>
        <button className="bg-secondary border border-border text-foreground px-5 py-2 text-sm rounded hover:bg-secondary/80 transition-colors">
          Uredi plan
        </button>
        <button className="bg-secondary border border-border text-foreground px-5 py-2 text-sm rounded hover:bg-secondary/80 transition-colors">
          Istorija
        </button>
      </div>

      {/* Week Overview Header */}
      <div className="bg-secondary border border-border rounded-lg p-4 mb-4">
        <div className="flex justify-between items-center">
          <div className="text-sm font-medium text-foreground">Nedelja: 22.03.2026 – 28.03.2026</div>
          <div className="flex gap-2">
            <button className="bg-card border border-border text-foreground px-3 py-1 text-sm rounded hover:bg-secondary transition-colors">
              ← Prethodna
            </button>
            <button className="bg-card border border-border text-foreground px-3 py-1 text-sm rounded hover:bg-secondary transition-colors">
              Sledeća →
            </button>
          </div>
        </div>
      </div>

      {/* Weekly Workout Grid */}
      <div className="grid grid-cols-7 gap-2 mb-6">
        {/* Monday - active day */}
        <div className="bg-card border border-border rounded-lg overflow-hidden">
          <div className="bg-primary p-2 text-center">
            <div className="text-xs font-semibold text-white">Ponedeljak</div>
            <div className="text-xs text-white/80 mt-0.5">22.03</div>
          </div>
          <div className="p-2.5 space-y-1.5">
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Bench Press</div>
              <div className="text-xs text-primary font-semibold">3×10</div>
            </div>
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Squats</div>
              <div className="text-xs text-primary font-semibold">4×8</div>
            </div>
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Pull-ups</div>
              <div className="text-xs text-primary font-semibold">3×12</div>
            </div>
          </div>
          <div className="border-t border-border p-2 text-xs text-muted-foreground text-center">
            Snaga · 60 min
          </div>
        </div>

        {/* Tuesday */}
        <div className="bg-card border border-border rounded-lg overflow-hidden">
          <div className="bg-secondary p-2 text-center">
            <div className="text-xs font-semibold text-foreground">Utorak</div>
            <div className="text-xs text-muted-foreground mt-0.5">23.03</div>
          </div>
          <div className="p-2.5 space-y-1.5">
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Trčanje</div>
              <div className="text-xs text-muted-foreground">30 min</div>
            </div>
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Bicikl</div>
              <div className="text-xs text-muted-foreground">20 min</div>
            </div>
          </div>
          <div className="border-t border-border p-2 text-xs text-muted-foreground text-center">
            Kardio · 50 min
          </div>
        </div>

        {/* Wednesday - rest */}
        <div className="bg-card border border-border rounded-lg overflow-hidden">
          <div className="bg-secondary p-2 text-center">
            <div className="text-xs font-semibold text-foreground">Sreda</div>
            <div className="text-xs text-muted-foreground mt-0.5">24.03</div>
          </div>
          <div className="p-2.5">
            <div className="text-center text-xs text-muted-foreground italic py-6">
              Dan odmora
            </div>
          </div>
          <div className="border-t border-border p-2 text-xs text-muted-foreground text-center">
            Odmor
          </div>
        </div>

        {/* Thursday - active */}
        <div className="bg-card border border-border rounded-lg overflow-hidden">
          <div className="bg-primary p-2 text-center">
            <div className="text-xs font-semibold text-white">Četvrtak</div>
            <div className="text-xs text-white/80 mt-0.5">25.03</div>
          </div>
          <div className="p-2.5 space-y-1.5">
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Deadlift</div>
              <div className="text-xs text-primary font-semibold">3×8</div>
            </div>
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Shoulder Press</div>
              <div className="text-xs text-primary font-semibold">3×10</div>
            </div>
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Rows</div>
              <div className="text-xs text-primary font-semibold">3×12</div>
            </div>
          </div>
          <div className="border-t border-border p-2 text-xs text-muted-foreground text-center">
            Snaga · 55 min
          </div>
        </div>

        {/* Friday */}
        <div className="bg-card border border-border rounded-lg overflow-hidden">
          <div className="bg-secondary p-2 text-center">
            <div className="text-xs font-semibold text-foreground">Petak</div>
            <div className="text-xs text-muted-foreground mt-0.5">26.03</div>
          </div>
          <div className="p-2.5 space-y-1.5">
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Yoga</div>
              <div className="text-xs text-muted-foreground">45 min</div>
            </div>
          </div>
          <div className="border-t border-border p-2 text-xs text-muted-foreground text-center">
            Fleks. · 45 min
          </div>
        </div>

        {/* Saturday - active */}
        <div className="bg-card border border-border rounded-lg overflow-hidden">
          <div className="bg-primary p-2 text-center">
            <div className="text-xs font-semibold text-white">Subota</div>
            <div className="text-xs text-white/80 mt-0.5">27.03</div>
          </div>
          <div className="p-2.5 space-y-1.5">
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Leg Press</div>
              <div className="text-xs text-primary font-semibold">4×10</div>
            </div>
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Lunges</div>
              <div className="text-xs text-primary font-semibold">3×12</div>
            </div>
            <div className="bg-secondary rounded p-1.5">
              <div className="text-xs font-medium text-foreground">Calf Raises</div>
              <div className="text-xs text-primary font-semibold">3×15</div>
            </div>
          </div>
          <div className="border-t border-border p-2 text-xs text-muted-foreground text-center">
            Noge · 50 min
          </div>
        </div>

        {/* Sunday - rest */}
        <div className="bg-card border border-border rounded-lg overflow-hidden">
          <div className="bg-secondary p-2 text-center">
            <div className="text-xs font-semibold text-foreground">Nedelja</div>
            <div className="text-xs text-muted-foreground mt-0.5">28.03</div>
          </div>
          <div className="p-2.5">
            <div className="text-center text-xs text-muted-foreground italic py-6">
              Dan odmora
            </div>
          </div>
          <div className="border-t border-border p-2 text-xs text-muted-foreground text-center">
            Odmor
          </div>
        </div>
      </div>

      {/* Weekly Summary */}
      <div className="grid grid-cols-4 gap-4">
        <div className="bg-card border border-border rounded-lg p-4 text-center border-t-2 border-t-primary">
          <div className="text-xs text-muted-foreground mb-2">Ukupno treninga</div>
          <div
            className="text-3xl font-bold text-primary"
            style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
          >
            5
          </div>
        </div>
        <div className="bg-card border border-border rounded-lg p-4 text-center border-t-2 border-t-emerald-500">
          <div className="text-xs text-muted-foreground mb-2">Ukupno vreme</div>
          <div
            className="text-3xl font-bold text-emerald-400"
            style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
          >
            260 min
          </div>
        </div>
        <div className="bg-card border border-border rounded-lg p-4 text-center border-t-2 border-t-blue-500">
          <div className="text-xs text-muted-foreground mb-2">Prosečno trajanje</div>
          <div
            className="text-3xl font-bold text-blue-400"
            style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
          >
            52 min
          </div>
        </div>
        <div className="bg-card border border-border rounded-lg p-4 text-center border-t-2 border-t-muted-foreground">
          <div className="text-xs text-muted-foreground mb-2">Dana odmora</div>
          <div
            className="text-3xl font-bold text-foreground"
            style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
          >
            2
          </div>
        </div>
      </div>
    </>
  );
}
