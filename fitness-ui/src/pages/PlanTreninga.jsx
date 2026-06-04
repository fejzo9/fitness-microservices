import * as React from 'react';
import { api } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

const DAY_NAMES = ['Nedjelja', 'Ponedeljak', 'Utorak', 'Srijeda', 'Četvrtak', 'Petak', 'Subota'];

// Vraća datum ponedjeljka za datu sedmicu (offset 0 = tekuća, 1 = iduća)
function getWeekMonday(weekOffset = 0) {
  const today = new Date();
  const day = today.getDay(); // 0=ned, 1=pon...
  const diff = (day === 0 ? -6 : 1 - day); // koliko dana do ponedjeljka
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
      ime: DAY_NAMES[d.getDay()],
      datum: `${dd}.${mm}`,
      dateObj: d,
      tip: "Odmor",
      trajanje: 0,
      aktivan: true,
      vezbe: []
    };
  });
}

export function PlanTreninga() {
  const { user } = useAuth();
  const [weekOffset, setWeekOffset] = React.useState(0);
  const [dani, setDani] = React.useState(() => buildWeekDays(0));
  const [sveVezbe, setSveVezbe] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [stats, setStats] = React.useState(null);

  // Stanja za interfejs
  const [prikaziFormu, setPrikaziFormu] = React.useState(false);
  const [modZaUredjivanje, setModZaUredjivanje] = React.useState(false);

  // Stanja za formu dodavanja
  const [izabraniDan, setIzabraniDan] = React.useState("");
  const [izabranaVezbaId, setIzabranaVezbaId] = React.useState("");
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
      const nextWeek = offset === 1;
      const [exercises, allEx, statistics] = await Promise.all([
        api.getWorkoutExercises(user.id, nextWeek),
        api.getExercises(0, 100),
        api.getWorkoutStatistics(user.id)
      ]);

      setSveVezbe(allEx.content || []);
      setStats(statistics);

      const weekDays = buildWeekDays(offset);

      // Mapiranje vežbi po scheduledDate
      const todayStr = (() => { const n = new Date(); const dd = String(n.getDate()).padStart(2,'0'); const mm = String(n.getMonth()+1).padStart(2,'0'); return `${n.getFullYear()}-${mm}-${dd}`; })();

      const noviDani = weekDays.map(dan => {
        const vezbeZaDan = (exercises || [])
          .filter(ex => ex.scheduledDate === dan.id)
          .sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''))
          .map(ex => ({
            id: ex.id,
            naziv: ex.exerciseName,
            detalji: `${ex.sets}×${ex.reps}`,
            startTime: ex.startTime ? ex.startTime.slice(0, 5) : null,
            restSec: ex.restSec,
            completed: ex.completed,
            canComplete: dan.id <= todayStr
          }));

        return {
          ...dan,
          vezbe: vezbeZaDan,
          trajanje: vezbeZaDan.length * 15,
          tip: vezbeZaDan.length > 0 ? "Snaga" : "Odmor"
        };
      });

      setDani(noviDani);
    } catch (error) {
      console.error("Error fetching workout data:", error);
    } finally {
      setLoading(false);
    }
  };

  // Funkcija za dodavanje vježbe
  const handleDodajVezbu = async (e) => {
    e.preventDefault();
    if (!izabranaVezbaId || !izabraniDan) return;

    const request = {
      userId: user.id,
      exerciseId: parseInt(izabranaVezbaId),
      scheduledDate: izabraniDan,
      sets: parseInt(sets) || 3,
      reps: parseInt(reps) || 10,
      startTime: startTime ? startTime + ":00" : "10:00:00",
      restSec: parseInt(restSec) || 60,
      completed: false
    };

    try {
      await api.createWorkoutExercise(request);
      await fetchData(weekOffset);

      // Reset forme
      setIzabranaVezbaId("");
      setSets("3");
      setReps("10");
      setStartTime("10:00");
      setRestSec("60");
      setPrikaziFormu(false);
    } catch (error) {
      console.error("Error creating workout exercise:", error);
    }
  };

  // Funkcija za označavanje vježbe kao završene
  const oznaciCompleted = async (vezbaId) => {
    try {
      await api.completeWorkoutExercise(vezbaId);
      await fetchData(weekOffset);
    } catch (error) {
      console.error("Error completing workout exercise:", error);
    }
  };

  // Funkcija za brisanje pojedinačne vežbe
  const obrisiVezbu = async (danId, vezbaId) => {
    try {
      await api.deleteWorkoutExercise(vezbaId);
      await fetchData(weekOffset);
    } catch (error) {
      console.error("Error deleting workout exercise:", error);
    }
  };

  // Funkcija za promenu trajanja direktno u uređivanju
  const promeniTrajanje = (danId, novoTrajanje) => {
    setDani((prethodniDani) =>
        prethodniDani.map((dan) =>
            dan.id === danId ? { ...dan, trajanje: Number(novoTrajanje) || 0 } : dan
        )
    );
  };

  // Proračun statistike uživo
  const treninziSaVezbama = dani.filter(dan => dan.vezbe.length > 0);
  const ukupnoTreninga = treninziSaVezbama.length;
  const ukupnoVreme = dani.reduce((acc, dan) => acc + dan.trajanje, 0);
  const prosecnoTrajanje = ukupnoTreninga > 0 ? Math.round(ukupnoVreme / ukupnoTreninga) : 0;
  const danaOdmora = dani.filter(dan => dan.vezbe.length === 0).length;

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

        {/* Action Buttons */}
        <div className="flex gap-3 mb-6">
          <button
              onClick={() => { setPrikaziFormu(!prikaziFormu); setModZaUredjivanje(false); if (!prikaziFormu) setIzabraniDan(new Date().toISOString().slice(0, 10)); }}
              type="button"
              className={`px-5 py-2 text-sm rounded font-medium transition-colors cursor-pointer ${prikaziFormu ? 'bg-red-600 text-white' : 'bg-primary text-white hover:bg-primary/90'}`}
          >
            {prikaziFormu ? "Zatvori" : "+ Nova vježba"}
          </button>
          <button
              onClick={() => { setModZaUredjivanje(!modZaUredjivanje); setPrikaziFormu(false); }}
              type="button"
              className={`px-5 py-2 text-sm rounded font-medium border transition-colors cursor-pointer ${modZaUredjivanje ? 'bg-amber-600 text-white border-transparent' : 'bg-secondary border-border text-foreground hover:bg-secondary/80'}`}
          >
            {modZaUredjivanje ? "Završi uređivanje" : "Uredi plan"}
          </button>
          <button type="button" className="bg-secondary border border-border text-foreground px-5 py-2 text-sm rounded hover:bg-secondary/80 transition-colors">
            Historija
          </button>
        </div>

        {/* Režim obaveštenja za uređivanje */}
        {modZaUredjivanje && (
            <div className="bg-amber-500/10 border border-amber-500/30 text-amber-500 rounded-lg p-3 mb-6 text-sm flex justify-between items-center animate-pulse">
              <span>U režimu ste za uređivanje. Možete uklanjati vježbe sa crvenim "X" i menjati minutažu na dnu svakog dana.</span>
              <button type="button" onClick={() => setModZaUredjivanje(false)} className="underline text-xs font-bold cursor-pointer">Završi</button>
            </div>
        )}

        {/* Forma za unos vježbe */}
        {prikaziFormu && (
            <form onSubmit={handleDodajVezbu} className="bg-card border border-primary/40 rounded-lg p-4 mb-6 shadow-md">
              <h3 className="text-sm font-semibold text-foreground mb-3">Nova vježba</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-3">
                <div>
                  <label className="block text-xs text-muted-foreground mb-1 font-medium">Datum</label>
                  <input
                      type="date"
                      value={izabraniDan}
                      onChange={(e) => setIzabraniDan(e.target.value)}
                      className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                      required
                  />
                </div>
                <div>
                  <label className="block text-xs text-muted-foreground mb-1 font-medium">Naziv vježbe</label>
                  <select
                      value={izabranaVezbaId}
                      onChange={(e) => setIzabranaVezbaId(e.target.value)}
                      className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                      required
                  >
                    <option value="">Izaberi vježbu</option>
                    {sveVezbe.map(v => (
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

        {/* Week Overview Header */}
        <div className="bg-secondary border border-border rounded-lg p-4 mb-4">
          <div className="flex justify-between items-center">
            <div className="text-sm font-medium text-foreground">
              {dani.length > 0 && `Sedmica: ${dani[0].datum} – ${dani[6].datum}`}
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

        {/* Weekly Workout Grid */}
        <div className="grid grid-cols-1 md:grid-cols-7 gap-2 mb-6">
          {dani.map((dan) => {
            const imaVezbi = dan.vezbe.length > 0;
            return (
                <div key={dan.id} className={`bg-card border rounded-lg overflow-hidden flex flex-col justify-between min-h-[240px] transition-all ${modZaUredjivanje ? 'border-amber-500/50 shadow-sm' : 'border-border'}`}>
                  <div>
                    <div className={`${dan.aktivan && imaVezbi ? "bg-primary text-white" : "bg-secondary text-foreground"} p-2 text-center`}>
                      <div className="text-xs font-semibold">{dan.ime}</div>
                      <div className={`text-xs mt-0.5 ${dan.aktivan && imaVezbi ? "text-white/80" : "text-muted-foreground"}`}>{dan.datum}</div>
                    </div>

                    <div className="p-2.5 space-y-1.5">
                      {imaVezbi ? (
                          dan.vezbe.map((vezba) => (
                              <div key={vezba.id} className="bg-secondary rounded p-1.5 flex justify-between items-start group relative">
                                <div className="flex-1 min-w-0">
                                  {vezba.startTime && (
                                    <div className="text-xs text-muted-foreground mb-0.5">{vezba.startTime}</div>
                                  )}
                                  <div className={`text-xs font-medium ${vezba.completed ? 'text-emerald-500 line-through' : 'text-foreground'}`}>
                                    {vezba.naziv || 'Vježba'}
                                  </div>
                                  <div className="text-xs text-primary font-semibold mt-0.5">{vezba.detalji}</div>
                                </div>
                                <div className="flex gap-1 ml-1 shrink-0">
                                  {!vezba.completed && vezba.canComplete && !modZaUredjivanje && (
                                    <button
                                      type="button"
                                      onClick={() => oznaciCompleted(vezba.id)}
                                      className="text-emerald-500 hover:text-emerald-700 text-xs font-bold px-1 rounded bg-emerald-500/10 hover:bg-emerald-500/20 cursor-pointer transition-colors"
                                      title="Označi kao završeno"
                                    >✓</button>
                                  )}
                                  {modZaUredjivanje && (
                                    <button
                                      type="button"
                                      onClick={() => obrisiVezbu(dan.id, vezba.id)}
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
                    {imaVezbi ? (
                        modZaUredjivanje ? (
                            <div className="flex items-center justify-center gap-1">
                              <span>{dan.tip} · </span>
                              <input
                                  type="number"
                                  value={dan.trajanje}
                                  onChange={(e) => promeniTrajanje(dan.id, e.target.value)}
                                  className="w-12 bg-card border border-border rounded text-center text-foreground p-0.5"
                                  min="0"
                              />
                              <span>min</span>
                            </div>
                        ) : (
                            `${dan.tip} · ${dan.trajanje} min`
                        )
                    ) : (
                        "Odmor"
                    )}
                  </div>
                </div>
            );
          })}
        </div>

        {/* Weekly Summary */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
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
              {dani.filter(d => d.vezbe.length === 0).length}
            </div>
          </div>
        </div>
      </div>
  );
}