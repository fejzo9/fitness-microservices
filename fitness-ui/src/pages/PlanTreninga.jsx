import * as React from 'react';
import { api } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../contexts/ToastContext';

const DAY_NAMES = ['Nedjelja', 'Ponedeljak', 'Utorak', 'Srijeda', 'Četvrtak', 'Petak', 'Subota'];

function getWeekMonday(weekOffset = 0) {
  const today = new Date();
  const day = today.getDay();
  const diff = (day === 0 ? -6 : 1 - day);
  const monday = new Date(today);
  monday.setDate(today.getDate() + diff + weekOffset * 7);
  monday.setHours(0, 0, 0, 0);
  return monday;
}

function buildWeekDays(weekOffset = 0) {
  const monday = getWeekMonday(weekOffset);
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const yyyy = d.getFullYear();
    return {
      id: `${yyyy}-${mm}-${dd}`, // "YYYY-MM-DD"
      name: DAY_NAMES[d.getDay()],
      date: `${dd}.${mm}`,
      dateObj: d,
      type: "Odmor",
      duration: 0,
      active: true,
      exercises: []
    };
  });
}

const todayStr = (() => { const n = new Date(); const dd = String(n.getDate()).padStart(2,'0'); const mm = String(n.getMonth()+1).padStart(2,'0'); return `${n.getFullYear()}-${mm}-${dd}`; })();

export function PlanTreninga() {
  const { user } = useAuth();
  const toast = useToast();
  const [weekOffset, setWeekOffset] = React.useState(0);
  const [days, setDays] = React.useState(() => buildWeekDays(0));
  const [allExercises, setAllExercises] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [stats, setStats] = React.useState(null);

  const [showHistory, setShowHistory] = React.useState(false);
  const [historyData, setHistoryData] = React.useState([]);
  const [historyPage, setHistoryPage] = React.useState(0);
  const [historyTotalPages, setHistoryTotalPages] = React.useState(0);
  const [historyLoading, setHistoryLoading] = React.useState(false);

  const [showForm, setShowForm] = React.useState(false);
  const [editMode, setEditMode] = React.useState(false);

  const [selectedDay, setSelectedDay] = React.useState("");
  const [selectedExerciseId, setSelectedExerciseId] = React.useState("");
  const [sets, setSets] = React.useState("3");
  const [reps, setReps] = React.useState("10");
  const [startTime, setStartTime] = React.useState("10:00");
  const [restSec, setRestSec] = React.useState("60");

  React.useEffect(() => {
    if (user?.id) {
      fetchData(weekOffset);
    }
  }, [user, weekOffset]);

  const fetchData = async (offset) => {
    setLoading(true);
    try {
      const nextWeek = offset > 0;
      const [exercises, allEx, statistics] = await Promise.all([
        api.getWorkoutExercises(user.id, nextWeek),
        api.getExercises(0, 100),
        api.getWorkoutStatistics(user.id)
      ]);

      setAllExercises(allEx.content || []);
      setStats(statistics);

      const weekDays = buildWeekDays(offset);

      const newDays = weekDays.map(day => {
        const dayExercises = (exercises || [])
          .filter(ex => ex.scheduledDate === day.id)
          .sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''))
          .map(ex => ({
            id: ex.id,
            name: ex.exerciseName,
            details: `${ex.sets}×${ex.reps}`,
            startTime: ex.startTime ? ex.startTime.slice(0, 5) : null,
            restSec: ex.restSec,
            completed: ex.completed,
            canComplete: day.id <= todayStr
          }));

        return {
          ...day,
          exercises: dayExercises,
          duration: dayExercises.length * 15,
          type: dayExercises.length > 0 ? "Snaga" : "Odmor"
        };
      });

      setDays(newDays);
    } catch (error) {
      toast('Greška pri učitavanju plana treninga. Provjerite konekciju i pokušajte ponovo.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleAddExercise = async (e) => {
    e.preventDefault();
    if (!selectedExerciseId || !selectedDay) return;

    const request = {
      userId: user.id,
      exerciseId: parseInt(selectedExerciseId),
      scheduledDate: selectedDay,
      sets: parseInt(sets) || 3,
      reps: parseInt(reps) || 10,
      startTime: startTime ? startTime + ":00" : "10:00:00",
      restSec: parseInt(restSec) || 60,
      completed: false
    };

    try {
      await api.createWorkoutExercise(request);
      await fetchData(weekOffset);

      setSelectedExerciseId("");
      setSets("3");
      setReps("10");
      setStartTime("10:00");
      setRestSec("60");
      setShowForm(false);
    } catch (error) {
      toast('Nije moguće dodati vježbu u plan. Pokušajte ponovo.', 'error');
    }
  };

  const markAsCompleted = async (exerciseId) => {
    try {
      await api.completeWorkoutExercise(exerciseId);
      await fetchData(weekOffset);
    } catch (error) {
      toast('Nije moguće označiti vježbu kao završenu. Pokušajte ponovo.', 'error');
    }
  };

  const deleteExercise = async (dayId, exerciseId) => {
    try {
      await api.deleteWorkoutExercise(exerciseId);
      await fetchData(weekOffset);
    } catch (error) {
      toast('Nije moguće obrisati vježbu iz plana. Pokušajte ponovo.', 'error');
    }
  };

  const fetchHistory = async (page = 0) => {
    if (!user?.id) return;
    setHistoryLoading(true);
    try {
      const data = await api.getWorkoutHistory(user.id, page, 10);
      setHistoryData(data.content || []);
      setHistoryTotalPages(data.totalPages || 0);
      setHistoryPage(page);
    } catch (error) {
      toast('Greška pri učitavanju historije treninga.', 'error');
    } finally {
      setHistoryLoading(false);
    }
  };

  const toggleHistory = () => {
    const newState = !showHistory;
    setShowHistory(newState);
    if (newState) {
      fetchHistory(0);
    }
  };

  const changeDuration = (dayId, newDuration) => {
    setDays((prev) =>
        prev.map((day) =>
            day.id === dayId ? { ...day, duration: Number(newDuration) || 0 } : day
        )
    );
  };

  const workoutsWithExercises = days.filter(day => day.exercises.length > 0);
  const totalWorkouts = workoutsWithExercises.length;
  const totalTime = days.reduce((acc, day) => acc + day.duration, 0);
  const avgDuration = totalWorkouts > 0 ? Math.round(totalTime / totalWorkouts) : 0;
  const restDays = days.filter(day => day.exercises.length === 0).length;

  return (
      <div className="p-4 bg-background text-foreground min-h-screen">
        {/* Page Title */}
        <div className="mb-6">
          <h2
              className="text-3xl font-bold uppercase tracking-wide border-b border-border pb-3"
              style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
          >
            Nedjeljni plan treninga
          </h2>
        </div>

        <div className="flex flex-wrap gap-2 mb-6">
          <button
              onClick={() => { setShowForm(!showForm); setEditMode(false); if (!showForm) setSelectedDay(new Date().toISOString().slice(0, 10)); }}
              type="button"
              className={`px-5 py-2 text-sm rounded font-medium transition-colors cursor-pointer flex-shrink-0 min-h-[44px] ${showForm ? 'bg-red-600 text-white' : 'bg-primary text-white hover:bg-primary/90'}`}
          >
            {showForm ? "Zatvori" : "+ Nova vježba"}
          </button>
          <button
              onClick={() => { setEditMode(!editMode); setShowForm(false); }}
              type="button"
              className={`px-5 py-2 text-sm rounded font-medium border transition-colors cursor-pointer flex-shrink-0 min-h-[44px] ${editMode ? 'bg-amber-600 text-white border-transparent' : 'bg-secondary border-border text-foreground hover:bg-secondary/80'}`}
          >
            {editMode ? "Završi uređivanje" : "Uredi plan"}
          </button>
          <button
              onClick={toggleHistory}
              type="button"
              className={`px-5 py-2 text-sm rounded font-medium border transition-colors cursor-pointer flex-shrink-0 min-h-[44px] ${showHistory ? 'bg-primary text-white border-transparent' : 'bg-secondary border-border text-foreground hover:bg-secondary/80'}`}
          >
            {showHistory ? "Zatvori historiju" : "Historija"}
          </button>
        </div>

        {/* Režim obaveštenja za uređivanje */}
        {!showHistory && editMode && (
            <div className="bg-amber-500/10 border border-amber-500/30 text-amber-500 rounded-lg p-3 mb-6 text-sm flex justify-between items-center animate-pulse">
              <span>U režimu ste za uređivanje. Možete uklanjati vježbe sa crvenim "X" i menjati minutažu na dnu svakog dana.</span>
              <button type="button" onClick={() => setEditMode(false)} className="underline text-xs font-bold cursor-pointer">Završi</button>
            </div>
        )}

        {/* Forma za unos vježbe */}
        {!showHistory && showForm && (
            <form onSubmit={handleAddExercise} className="bg-card border border-primary/40 rounded-lg p-4 mb-6 shadow-md">
              <h3 className="text-sm font-semibold text-foreground mb-3">Nova vježba</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-3">
                <div>
                  <label className="block text-xs text-muted-foreground mb-1 font-medium">Datum</label>
                  <input
                      type="date"
                      value={selectedDay}
                      onChange={(e) => setSelectedDay(e.target.value)}
                      className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                      required
                  />
                </div>
                <div>
                  <label className="block text-xs text-muted-foreground mb-1 font-medium">Naziv vježbe</label>
                  <select
                      value={selectedExerciseId}
                      onChange={(e) => setSelectedExerciseId(e.target.value)}
                      className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                      required
                  >
                    <option value="">Izaberi vježbu</option>
                    {allExercises.map(v => (
                        <option key={v.id} value={v.id}>{v.name}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs text-muted-foreground mb-1 font-medium">Vrijeme početka</label>
                  <input
                      type="time"
                      value={startTime}
                      onChange={(e) => setStartTime(e.target.value)}
                      className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                  />
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
                <div>
                  <label className="block text-xs text-muted-foreground mb-1 font-medium">Serije</label>
                  <input
                      type="number"
                      value={sets}
                      onChange={(e) => setSets(e.target.value)}
                      className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                      min="1"
                      required
                  />
                </div>
                <div>
                  <label className="block text-xs text-muted-foreground mb-1 font-medium">Ponavljanja</label>
                  <input
                      type="number"
                      value={reps}
                      onChange={(e) => setReps(e.target.value)}
                      className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                      min="1"
                      required
                  />
                </div>
                <div>
                  <label className="block text-xs text-muted-foreground mb-1 font-medium">Odmor (sekunde)</label>
                  <input
                      type="number"
                      value={restSec}
                      onChange={(e) => setRestSec(e.target.value)}
                      className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                      min="0"
                  />
                </div>
                <button type="submit" className="bg-primary text-white text-sm font-medium rounded p-2 hover:bg-primary/90 transition-colors cursor-pointer">
                  Sačuvaj vježbu
                </button>
              </div>
            </form>
        )}

        {/* Historija vježbi */}
        {showHistory && (
            <div className="bg-card border border-border rounded-lg overflow-hidden mb-6 shadow-sm">
              <div className="bg-secondary/50 p-4 border-b border-border flex justify-between items-center">
                <h3 className="font-bold text-lg uppercase tracking-wider" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                  Sve dodane vježbe (Historija)
                </h3>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-secondary/30 border-b border-border">
                      <th className="p-3 text-xs font-bold uppercase text-muted-foreground tracking-wider">Datum</th>
                      <th className="p-3 text-xs font-bold uppercase text-muted-foreground tracking-wider">Vježba</th>
                      <th className="p-3 text-xs font-bold uppercase text-muted-foreground tracking-wider text-center">Serije</th>
                      <th className="p-3 text-xs font-bold uppercase text-muted-foreground tracking-wider text-center">Ponavljanja</th>
                      <th className="p-3 text-xs font-bold uppercase text-muted-foreground tracking-wider text-center">Odmor (s)</th>
                      <th className="p-3 text-xs font-bold uppercase text-muted-foreground tracking-wider text-center">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border/50">
                    {historyLoading ? (
                        <tr><td colSpan="6" className="p-8 text-center text-muted-foreground">Učitavanje historije...</td></tr>
                    ) : historyData.length > 0 ? (
                        historyData.map((ex) => (
                            <tr key={ex.id} className="hover:bg-secondary/20 transition-colors">
                              <td className="p-3 text-sm font-medium text-foreground">{ex.scheduledDate}</td>
                              <td className="p-3 text-sm text-foreground">{ex.exerciseName}</td>
                              <td className="p-3 text-sm text-foreground text-center">{ex.sets}</td>
                              <td className="p-3 text-sm text-foreground text-center">{ex.reps}</td>
                              <td className="p-3 text-sm text-foreground text-center">{ex.restSec || 0}</td>
                              <td className="p-3 text-sm text-center">
                                {ex.completed ? (
                                    <span className="bg-emerald-500/10 text-emerald-500 text-[10px] font-bold uppercase px-2 py-0.5 rounded border border-emerald-500/20">Završeno</span>
                                ) : (
                                    <span className="bg-amber-500/10 text-amber-500 text-[10px] font-bold uppercase px-2 py-0.5 rounded border border-amber-500/20">Planirano</span>
                                )}
                              </td>
                            </tr>
                        ))
                    ) : (
                        <tr><td colSpan="6" className="p-8 text-center text-muted-foreground italic">Nema zapisa u historiji.</td></tr>
                    )}
                  </tbody>
                </table>
              </div>
              
              {/* Paginacija */}
              {historyTotalPages > 1 && (
                  <div className="bg-secondary/30 p-3 border-t border-border flex items-center justify-between">
                    <div className="text-xs text-muted-foreground">
                      Stranica {historyPage + 1} od {historyTotalPages}
                    </div>
                    <div className="flex gap-2">
                      <button
                          onClick={() => fetchHistory(historyPage - 1)}
                          disabled={historyPage === 0 || historyLoading}
                          className="px-3 py-1 bg-card border border-border text-foreground text-xs rounded hover:bg-secondary transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                      >
                        Prethodna
                      </button>
                      <button
                          onClick={() => fetchHistory(historyPage + 1)}
                          disabled={historyPage === historyTotalPages - 1 || historyLoading}
                          className="px-3 py-1 bg-card border border-border text-foreground text-xs rounded hover:bg-secondary transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                      >
                        Sljedeća
                      </button>
                    </div>
                  </div>
              )}
            </div>
        )}

        {/* Week Overview Header */}
        {!showHistory && (
          <div className="bg-secondary border border-border rounded-lg p-4 mb-4">
            <div className="flex flex-wrap justify-between items-center gap-2">
              <div className="text-sm font-medium text-foreground">
                {days.length > 0 && `Sedmica: ${days[0].date} – ${days[6].date}`}
                {weekOffset === 0 && <span className="ml-2 text-xs text-primary">(tekuća)</span>}
                {weekOffset === 1 && <span className="ml-2 text-xs text-emerald-400">(iduća)</span>}
              </div>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setWeekOffset(o => Math.max(0, o - 1))}
                  disabled={weekOffset === 0}
                  className="bg-card border border-border text-foreground px-3 py-1 text-sm rounded hover:bg-secondary transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  ← Prethodna
                </button>
                <button
                  type="button"
                  onClick={() => setWeekOffset(o => Math.min(1, o + 1))}
                  disabled={weekOffset === 1}
                  className="bg-card border border-border text-foreground px-3 py-1 text-sm rounded hover:bg-secondary transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  Sledeća →
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Weekly Workout Grid */}
        {!showHistory && (
          <div className="overflow-x-auto -mx-4 px-4 md:mx-0 md:px-0">
          <div className="grid grid-cols-7 gap-2 mb-6 min-w-[700px]">
            {days.map((day) => {
              const hasExercises = day.exercises.length > 0;
              return (
                  <div key={day.id} className={`bg-card border rounded-lg overflow-hidden flex flex-col justify-between min-h-[240px] transition-all ${editMode ? 'border-amber-500/50 shadow-sm' : 'border-border'}`}>
                    <div>
                      <div className={`${day.active && hasExercises ? "bg-primary text-white" : "bg-secondary text-foreground"} p-2 text-center`}>
                        <div className="text-xs font-semibold">{day.name}</div>
                        <div className={`text-xs mt-0.5 ${day.active && hasExercises ? "text-white/80" : "text-muted-foreground"}`}>{day.date}</div>
                      </div>

                      <div className="p-2.5 space-y-1.5">
                        {hasExercises ? (
                            day.exercises.map((exercise) => (
                                <div key={exercise.id} className="bg-secondary rounded p-1.5 flex justify-between items-start group relative">
                                  <div className="flex-1 min-w-0">
                                    {exercise.startTime && (
                                      <div className="text-xs text-muted-foreground mb-0.5">{exercise.startTime}</div>
                                    )}
                                    <div className={`text-xs font-medium ${exercise.completed ? 'text-emerald-500 line-through' : 'text-foreground'}`}>
                                      {exercise.name || 'Vježba'}
                                    </div>
                                    <div className="text-xs text-primary font-semibold mt-0.5">{exercise.details}</div>
                                  </div>
                                  <div className="flex gap-1 ml-1 shrink-0">
                                    {!exercise.completed && exercise.canComplete && !editMode && (
                                      <button
                                        type="button"
                                        onClick={() => markAsCompleted(exercise.id)}
                                        className="text-emerald-500 hover:text-emerald-700 text-xs font-bold px-1 rounded bg-emerald-500/10 hover:bg-emerald-500/20 cursor-pointer transition-colors"
                                        title="Označi kao završeno"
                                      >✓</button>
                                    )}
                                    {editMode && day.id >= todayStr && (
                                      <button
                                        type="button"
                                        onClick={() => deleteExercise(day.id, exercise.id)}
                                        className="text-red-500 hover:text-red-700 text-xs font-bold px-1 rounded bg-red-500/10 hover:bg-red-500/20 cursor-pointer transition-colors"
                                        title="Obriši vježbu"
                                      >✕</button>
                                    )}
                                  </div>
                                </div>
                            ))
                        ) : (
                            <div className="text-center text-xs text-muted-foreground italic py-6">
                              Dan odmora
                            </div>
                        )}
                      </div>
                    </div>

                    {/* Donji deo kartice sa vremenom */}
                    <div className="border-t border-border p-2 text-xs text-muted-foreground text-center bg-secondary/30">
                      {hasExercises ? (
                          editMode ? (
                              <div className="flex items-center justify-center gap-1">
                                <span>{day.type} · </span>
                                <input
                                    type="number"
                                    value={day.duration}
                                    onChange={(e) => changeDuration(day.id, e.target.value)}
                                    className="w-12 bg-card border border-border rounded text-center text-foreground p-0.5"
                                    min="0"
                                />
                                <span>min</span>
                              </div>
                          ) : (
                              `${day.type} · ${day.duration} min`
                          )
                      ) : (
                          "Odmor"
                      )}
                    </div>
                  </div>
              );
            })}
          </div>

          </div>
        )}

        {/* Weekly Summary */}
        {!showHistory && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-4">
            <div className="bg-card border border-border rounded-lg p-4 text-center border-t-2 border-t-primary">
              <div className="text-xs text-muted-foreground mb-2">Ukupno treninga</div>
              <div className="text-3xl font-bold text-primary" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                {stats?.totalPlannedExercises || 0}
              </div>
            </div>
            <div className="bg-card border border-border rounded-lg p-4 text-center border-t-2 border-t-emerald-500">
              <div className="text-xs text-muted-foreground mb-2">Završeno</div>
              <div className="text-3xl font-bold text-emerald-400" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                {stats?.totalCompletedExercises || 0}
              </div>
            </div>
            <div className="bg-card border border-border rounded-lg p-4 text-center border-t-2 border-t-blue-500">
              <div className="text-xs text-muted-foreground mb-2">Procenat</div>
              <div className="text-3xl font-bold text-blue-400" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                {stats?.completionPercentage || 0}%
              </div>
            </div>
            <div className="bg-card border border-border rounded-lg p-4 text-center border-t-2 border-t-muted-foreground">
              <div className="text-xs text-muted-foreground mb-2">Dana odmora</div>
              <div className="text-3xl font-bold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                {days.filter(d => d.exercises.length === 0).length}
              </div>
            </div>
          </div>
        )}
      </div>
  );
}