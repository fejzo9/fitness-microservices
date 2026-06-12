import { useState, useEffect, useRef } from 'react';
import { api } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { Spinner } from '../components/Spinner';
import { useToast } from '../contexts/ToastContext';
import { Input } from '../components/Input';

export function Dashboard() {
  const { user, refreshUser } = useAuth();
  const toast = useToast();
  const [todayExercises, setTodayExercises] = useState([]);
  const [stats, setStats] = useState(null);
  const [todayMeals, setTodayMeals] = useState([]);
  const [loading, setLoading] = useState(true);

  const [showWeightModal, setShowWeightModal] = useState(false);
  const [newWeight, setNewWeight] = useState(user?.weight || '');
  const [entryDate, setEntryDate] = useState(new Date().toISOString().split('T')[0]);
  const [savingWeight, setSavingWeight] = useState(false);
  const modalRef = useRef();

  useEffect(() => {
    if (user?.id) {
      fetchDashboardData();
    }
  }, [user?.id]);

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
      
      const sorted = (ex || []).sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''));
      setTodayExercises(sorted);
      setStats(statistics);
      setTodayMeals(meals || []);
    } catch (error) {
      toast('Greška pri učitavanju podataka. Provjerite konekciju i pokušajte ponovo.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = async (id) => {
    try {
      await api.completeWorkoutExercise(id);
      await fetchDashboardData();
    } catch (error) {
      toast('Nije moguće označiti vježbu kao završenu. Pokušajte ponovo.', 'error');
    }
  };

  const handleWeightSubmit = async (e) => {
    e.preventDefault();
    if (!newWeight || !entryDate) return;

    setSavingWeight(true);
    try {
      await api.addWeightEntry({
        userId: user.id,
        weight: parseInt(newWeight),
        entryDate: entryDate
      });

      // Refresh local user state in context
      await refreshUser();
      
      toast('Tjelesna težina uspješno evidentirana.', 'success');
      setShowWeightModal(false);
    } catch (error) {
      toast('Greška pri spremanju težine.', 'error');
    } finally {
      setSavingWeight(false);
    }
  };

  return (
    <div className="p-4 md:p-6 max-w-7xl mx-auto">
      {/* Page Title */}
      <div className="mb-6 flex justify-between items-center border-b border-border pb-3">
        <h2 className="text-xl font-normal">Dashboard</h2>
        <div className="flex items-center gap-4">
          <div className="text-sm">
            <span className="text-muted-foreground">Težina: </span>
            <span className="font-bold text-primary">{user?.weight || '--'} kg</span>
          </div>
          <button
            onClick={() => setShowWeightModal(true)}
            className="bg-primary text-primary-foreground px-3 py-1 text-sm hover:bg-primary/90 transition-colors"
          >
            + Unesi težinu
          </button>
        </div>
      </div>


      {/* Dashboard Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6 mb-6">
        {/* Today's Workout Plan Card */}
        <div className="border-2 border-border bg-card p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3 className="text-base font-normal">Današnji trening</h3>
          </div>
          <div className="space-y-3">
            {loading ? (
              <Spinner size="md" className="py-8" />
            ) : todayExercises.length > 0 ? (
              todayExercises.map(ex => (
                <div key={ex.id} className="flex justify-between items-start border-b border-border/50 pb-2">
                  <div className="flex-1 min-w-0">
                    {ex.startTime && (
                      <div className="text-xs text-muted-foreground">{ex.startTime.slice(0,5)}</div>
                    )}
                    <span className={`text-sm ${ex.completed ? 'text-emerald-500 line-through' : 'text-foreground'}`}>
                      {ex.exerciseName}
                    </span>
                    <div className="text-xs text-muted-foreground mt-0.5">
                      {ex.sets}×{ex.reps} serija{ex.restSec ? ` · odmor ${ex.restSec}s` : ''}
                    </div>
                  </div>
                  <div className="ml-2 shrink-0">
                    {ex.completed ? (
                      <span className="text-xs text-emerald-500 font-bold">✓</span>
                    ) : (
                      <button
                        type="button"
                        onClick={() => handleComplete(ex.id)}
                        className="bg-emerald-500 hover:bg-emerald-600 text-white text-[10px] font-bold px-2 py-1 rounded shadow-sm transition-all flex items-center justify-center min-w-[24px]"
                        title="Označi kao završeno"
                      >✓</button>
                    )}
                  </div>
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
            <Spinner size="md" className="py-8" />
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
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-6">
        {/* Weekly Statistics Card */}
        <div className="md:col-span-2 border-2 border-border bg-card p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3 className="text-base font-normal">Nedjeljne statistike</h3>
          </div>
          {loading ? (
             <Spinner size="md" className="py-8" />
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
                <div className="text-xl font-bold text-blue-400">{(stats.completionPercentage || 0).toFixed(2)}%</div>
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

      {/* Weight Entry Modal */}
      {showWeightModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div ref={modalRef} className="bg-card border-2 border-border w-full max-w-md p-6 shadow-2xl">
            <h3 className="text-xl font-normal border-b border-border pb-3 mb-5">Nova tjesna težina</h3>
            <form onSubmit={handleWeightSubmit} className="space-y-4">
              <Input
                label="Datum unosa"
                type="date"
                value={entryDate}
                onChange={(e) => setEntryDate(e.target.value)}
                required
                max={new Date().toISOString().split('T')[0]}
              />
              <Input
                label="Težina (kg)"
                type="number"
                value={newWeight}
                onChange={(e) => setNewWeight(e.target.value)}
                placeholder="npr. 85"
                required
                min="30"
                max="300"
              />
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowWeightModal(false)}
                  className="flex-1 bg-secondary text-foreground py-2 hover:bg-secondary/80 transition-colors border border-border"
                >
                  Odustani
                </button>
                <button
                  type="submit"
                  disabled={savingWeight}
                  className="flex-1 bg-primary text-primary-foreground py-2 hover:bg-primary/90 transition-colors disabled:opacity-50"
                >
                  {savingWeight ? 'Spremanje...' : 'Spremi'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
