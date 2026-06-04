import { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

export function Dashboard() {
  const { user } = useAuth();
  const [todayExercises, setTodayExercises] = useState([]);
  const [stats, setStats] = useState(null);
  const [todayMeals, setTodayMeals] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.id) {
      fetchDashboardData();
    }
  }, [user]);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
      const today = days[new Date().getDay()];
      const todayDate = new Date().toISOString().split('T')[0];

      const [ex, statistics, meals] = await Promise.all([
        api.getWorkoutExercisesByDay(user.id, today),
        api.getWorkoutStatistics(user.id),
        api.getMealEntriesByUserAndDate(user.id, todayDate)
      ]);

      setTodayExercises(ex || []);
      setStats(statistics);
      setTodayMeals(meals || []);
    } catch (error) {
      console.error("Error fetching dashboard data:", error);
    } finally {
      setLoading(false);
    }
  };

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
            {loading ? (
              <div className="text-center py-8 text-muted-foreground text-sm">Učitavanje...</div>
            ) : todayExercises.length > 0 ? (
              todayExercises.map(ex => (
                <div key={ex.id} className="flex justify-between items-center border-b border-border/50 pb-2">
                  <span className={`text-sm ${ex.completed ? 'text-emerald-500 line-through' : 'text-foreground'}`}>
                    {ex.exerciseName}
                  </span>
                  <span className="text-xs text-primary font-medium">{ex.sets}×{ex.reps}</span>
                </div>
              ))
            ) : (
              <div className="text-center py-8 text-muted-foreground text-sm">
                Nema planiranih treninga za danas
              </div>
            )}
          </div>
        </div>

        {/* Daily Calories Summary Card */}
        <div className="border-2 border-border bg-card p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3 className="text-base font-normal">Dnevne kalorije</h3>
          </div>
          {loading ? (
            <div className="text-center py-8 text-muted-foreground text-sm">Učitavanje...</div>
          ) : todayMeals.length > 0 ? (
            <div className="space-y-4">
              {/* Total Summary */}
              <div className="bg-secondary rounded-lg p-4">
                <div className="grid grid-cols-2 gap-3">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-primary" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                      {todayMeals.reduce((sum, m) => sum + (parseFloat(m.calories) || 0), 0).toFixed(0)}
                    </div>
                    <div className="text-xs text-muted-foreground">Ukupno kcal</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-emerald-400" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                      {todayMeals.reduce((sum, m) => sum + (parseFloat(m.proteinG) || 0), 0).toFixed(0)}g
                    </div>
                    <div className="text-xs text-muted-foreground">Proteini</div>
                  </div>
                </div>
              </div>

              {/* Meals List */}
              <div className="space-y-2">
                {todayMeals.map(meal => (
                  <div key={meal.id} className="flex justify-between items-center border-b border-border/50 pb-2">
                    <div className="flex-1">
                      <span className="text-sm text-foreground block">{meal.mealName}</span>
                      <span className="text-xs text-muted-foreground">{meal.mealTime?.slice(0, 5)}</span>
                    </div>
                    <span className="text-sm font-medium text-primary">{parseFloat(meal.calories || 0).toFixed(0)} kcal</span>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="text-center py-8 text-muted-foreground text-sm">
              Nema podataka o kalorijama za danas
            </div>
          )}
        </div>
      </div>

      {/* Second Row - Weekly Stats and Recent Activity */}
      <div className="grid grid-cols-3 gap-6">
        {/* Weekly Statistics Card */}
        <div className="col-span-2 border-2 border-border bg-card p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3 className="text-base font-normal">Nedjeljne statistike</h3>
          </div>
          {loading ? (
             <div className="text-center py-8 text-muted-foreground text-sm">Učitavanje...</div>
          ) : stats ? (
            <div className="grid grid-cols-3 gap-4">
              <div className="text-center">
                <div className="text-xs text-muted-foreground mb-1">Ukupno vježbi</div>
                <div className="text-xl font-bold text-primary">{stats.totalPlannedExercises}</div>
              </div>
              <div className="text-center">
                <div className="text-xs text-muted-foreground mb-1">Završeno</div>
                <div className="text-xl font-bold text-emerald-400">{stats.totalCompletedExercises}</div>
              </div>
              <div className="text-center">
                <div className="text-xs text-muted-foreground mb-1">Procenat</div>
                <div className="text-xl font-bold text-blue-400">{stats.completionPercentage}%</div>
              </div>
            </div>
          ) : (
            <div className="text-center py-8 text-muted-foreground text-sm">
              Nema podataka o nedjeljnim statistikama
            </div>
          )}
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
