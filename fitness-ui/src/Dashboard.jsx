export function Dashboard() {
  return (
    <div className="p-6 max-w-7xl mx-auto">
      {/* Page Title */}
      <div className="mb-6">
        <h2 className="text-xl font-normal border-b border-border pb-3">Dashboard</h2>
      </div>

      {/* Quick Action Buttons */}
      <div className="flex gap-4 mb-6">
        <button type="button" className="border-2 border-border bg-primary text-primary-foreground px-5 py-2 text-sm">
          + Add Workout
        </button>
        <button type="button" className="border-2 border-border bg-card text-card-foreground px-5 py-2 text-sm">
          + Add Meal
        </button>
        <button type="button" className="border-2 border-border bg-card text-card-foreground px-5 py-2 text-sm">
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
            <div className="text-center py-8 text-muted-foreground text-sm">
              Nema planiranih treninga za danas
            </div>
          </div>
        </div>

        {/* Daily Calories Summary Card */}
        <div className="border-2 border-border bg-card p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3 className="text-base font-normal">Dnevne kalorije</h3>
          </div>
          <div className="text-center py-8 text-muted-foreground text-sm">
            Nema podataka o kalorijama za danas
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
          <div className="text-center py-8 text-muted-foreground text-sm">
            Nema podataka o nedeljnim statistikama
          </div>
        </div>

        {/* Recent Activity Card */}
        <div className="border-2 border-border bg-card p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3 className="text-base font-normal">Aktivnosti</h3>
          </div>
          <div className="text-center py-8 text-muted-foreground text-sm">
            Nema nedavnih aktivnosti
          </div>
        </div>
      </div>
    </div>
  );
}
