export function Dashboard() {
  return (
    <div className="p-6 max-w-7xl mx-auto">
      {/* Page Title */}
      <div className="mb-6">
        <h2 className="text-xl font-normal border-b border-border pb-3">Dashboard</h2>
      </div>

      {/* Quick Action Buttons */}
      <div className="flex gap-4 mb-6">
        <button className="border-2 border-border bg-primary text-primary-foreground px-5 py-2 text-sm">
          + Add Workout
        </button>
        <button className="border-2 border-border bg-card text-card-foreground px-5 py-2 text-sm">
          + Add Meal
        </button>
        <button className="border-2 border-border bg-card text-card-foreground px-5 py-2 text-sm">
          + Add Progress Entry
        </button>
      </div>

      {/* Dashboard Cards Grid */}
      <div className="grid grid-cols-2 gap-6 mb-6">
        {/* Today's Workout Plan Card */}
        <div className="border-2 border-border bg-card p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3 className="text-base font-normal">Današnji trening</h3>
          </div>
          <div className="space-y-3">
            <div className="border border-border bg-muted p-3">
              <div className="flex justify-between items-center mb-1">
                <span className="text-sm">Bench Press</span>
                <span className="text-xs text-muted-foreground">3x10</span>
              </div>
              <div className="text-xs text-muted-foreground">Gornji deo tela</div>
            </div>
            <div className="border border-border bg-muted p-3">
              <div className="flex justify-between items-center mb-1">
                <span className="text-sm">Squats</span>
                <span className="text-xs text-muted-foreground">4x12</span>
              </div>
              <div className="text-xs text-muted-foreground">Noge</div>
            </div>
            <div className="border border-border bg-muted p-3">
              <div className="flex justify-between items-center mb-1">
                <span className="text-sm">Deadlift</span>
                <span className="text-xs text-muted-foreground">3x8</span>
              </div>
              <div className="text-xs text-muted-foreground">Leđa</div>
            </div>
          </div>
          <div className="mt-4 pt-3 border-t border-border text-xs text-muted-foreground">
            Trajanje: ~45 min
          </div>
        </div>

        {/* Daily Calories Summary Card */}
        <div className="border-2 border-border bg-card p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3 className="text-base font-normal">Dnevne kalorije</h3>
          </div>
          <div className="space-y-4">
            {/* Calorie Progress Bar */}
            <div>
              <div className="flex justify-between text-sm mb-2">
                <span>Uneto</span>
                <span className="font-normal">1,650 / 2,200 kcal</span>
              </div>
              <div className="border-2 border-border bg-muted h-8">
                <div className="bg-primary h-full" style={{width: '75%'}}></div>
              </div>
            </div>

            {/* Macros Breakdown */}
            <div className="border border-border bg-muted p-3 space-y-2">
              <div className="flex justify-between text-xs">
                <span>Proteini</span>
                <span className="text-muted-foreground">85g / 120g</span>
              </div>
              <div className="flex justify-between text-xs">
                <span>Ugljeni hidrati</span>
                <span className="text-muted-foreground">180g / 250g</span>
              </div>
              <div className="flex justify-between text-xs">
                <span>Masti</span>
                <span className="text-muted-foreground">45g / 70g</span>
              </div>
            </div>

            {/* Remaining calories */}
            <div className="text-center pt-2">
              <span className="text-sm text-muted-foreground">Preostalo: 550 kcal</span>
            </div>
          </div>
        </div>
      </div>

      {/* Second Row - Weekly Stats and Recent Activity */}
      <div className="grid grid-cols-3 gap-6">
        {/* Weekly Statistics Card */}
        <div className="col-span-2 border-2 border-border bg-card p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3 className="text-base font-normal">Nedeljne statistike</h3>
          </div>
          <div className="grid grid-cols-4 gap-4">
            <div className="border border-border bg-muted p-4 text-center">
              <div className="w-12 h-12 border-2 border-border bg-secondary mx-auto mb-2"></div>
              <div className="text-lg font-normal mb-1">5</div>
              <div className="text-xs text-muted-foreground">Treninzi</div>
            </div>
            <div className="border border-border bg-muted p-4 text-center">
              <div className="w-12 h-12 border-2 border-border bg-secondary mx-auto mb-2"></div>
              <div className="text-lg font-normal mb-1">12,500</div>
              <div className="text-xs text-muted-foreground">Kalorije</div>
            </div>
            <div className="border border-border bg-muted p-4 text-center">
              <div className="w-12 h-12 border-2 border-border bg-secondary mx-auto mb-2"></div>
              <div className="text-lg font-normal mb-1">240</div>
              <div className="text-xs text-muted-foreground">Minuta</div>
            </div>
            <div className="border border-border bg-muted p-4 text-center">
              <div className="w-12 h-12 border-2 border-border bg-secondary mx-auto mb-2"></div>
              <div className="text-lg font-normal mb-1">-0.5</div>
              <div className="text-xs text-muted-foreground">kg</div>
            </div>
          </div>
        </div>

        {/* Recent Activity Card */}
        <div className="border-2 border-border bg-card p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3 className="text-base font-normal">Aktivnosti</h3>
          </div>
          <div className="space-y-2">
            <div className="border border-border bg-muted p-2">
              <div className="text-xs font-normal mb-1">Snaga trening</div>
              <div className="text-xs text-muted-foreground">Danas, 09:30</div>
            </div>
            <div className="border border-border bg-muted p-2">
              <div className="text-xs font-normal mb-1">Doručak unos</div>
              <div className="text-xs text-muted-foreground">Danas, 08:00</div>
            </div>
            <div className="border border-border bg-muted p-2">
              <div className="text-xs font-normal mb-1">Težina merenje</div>
              <div className="text-xs text-muted-foreground">Juče, 07:15</div>
            </div>
            <div className="border border-border bg-muted p-2">
              <div className="text-xs font-normal mb-1">Kardio sesija</div>
              <div className="text-xs text-muted-foreground">Juče, 18:30</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
