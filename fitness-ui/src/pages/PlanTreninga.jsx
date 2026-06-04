import * as React from 'react';
import { api } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

const DAY_MAP = {
  'MONDAY': 'pon',
  'TUESDAY': 'uto',
  'WEDNESDAY': 'sre',
  'THURSDAY': 'cet',
  'FRIDAY': 'pet',
  'SATURDAY': 'sub',
  'SUNDAY': 'ned'
};

const REV_DAY_MAP = {
  'pon': 'MONDAY',
  'uto': 'TUESDAY',
  'sre': 'WEDNESDAY',
  'cet': 'THURSDAY',
  'pet': 'FRIDAY',
  'sub': 'SATURDAY',
  'ned': 'SUNDAY'
};

const INITIAL_DANI = [
  { id: "pon", ime: "Ponedeljak", datum: "22.03", tip: "Trening", trajanje: 0, aktivan: true, vezbe: [] },
  { id: "uto", ime: "Utorak", datum: "23.03", tip: "Trening", trajanje: 0, aktivan: true, vezbe: [] },
  { id: "sre", ime: "Sreda", datum: "24.03", tip: "Trening", trajanje: 0, aktivan: true, vezbe: [] },
  { id: "cet", ime: "Četvrtak", datum: "25.03", tip: "Trening", trajanje: 0, aktivan: true, vezbe: [] },
  { id: "pet", ime: "Petak", datum: "26.03", tip: "Trening", trajanje: 0, aktivan: true, vezbe: [] },
  { id: "sub", ime: "Subota", datum: "27.03", tip: "Trening", trajanje: 0, aktivan: true, vezbe: [] },
  { id: "ned", ime: "Nedelja", datum: "28.03", tip: "Trening", trajanje: 0, aktivan: true, vezbe: [] }
];

export function PlanTreninga() {
  const { user } = useAuth();
  // Stanje za dane i vežbe
  const [dani, setDani] = React.useState(INITIAL_DANI);
  const [sveVezbe, setSveVezbe] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [stats, setStats] = React.useState(null);

  // Stanja za interfejs
  const [prikaziFormu, setPrikaziFormu] = React.useState(false);
  const [modZaUredjivanje, setModZaUredjivanje] = React.useState(false);

  // Stanja za formu dodavanja
  const [izabraniDan, setIzabraniDan] = React.useState("pon");
  const [izabranaVezbaId, setIzabranaVezbaId] = React.useState("");
  const [detaljiVezbe, setDetaljiVezbe] = React.useState("");
  const [dodatnoTrajanje, setDodatnoTrajanje] = React.useState("0");

  React.useEffect(() => {
    if (user?.id) {
      fetchData();
    }
  }, [user]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [exercises, allEx, statistics] = await Promise.all([
        api.getWorkoutExercises(user.id),
        api.getExercises(0, 100),
        api.getWorkoutStatistics(user.id)
      ]);

      setSveVezbe(allEx.content || []);
      setStats(statistics);

      // Mapiranje vežbi po danima
      const noviDani = INITIAL_DANI.map(dan => {
        const vezbeZaDan = exercises
          .filter(ex => DAY_MAP[ex.dayOfWeek] === dan.id)
          .map(ex => ({
            id: ex.id,
            naziv: ex.exerciseName,
            detalji: `${ex.sets}×${ex.reps}`,
            completed: ex.completed
          }));
        
        return {
          ...dan,
          vezbe: vezbeZaDan,
          trajanje: vezbeZaDan.length * 15, // Aproksimacija trajanja
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

  // Funkcija za dodavanje vežbe
  const handleDodajVezbu = async (e) => {
    e.preventDefault();
    if (!izabranaVezbaId || !detaljiVezbe.trim()) return;

    const parts = detaljiVezbe.split(/[x×*]/i);
    const sets = parseInt(parts[0]) || 3;
    const reps = parseInt(parts[1]) || 10;

    const request = {
      userId: user.id,
      exerciseId: parseInt(izabranaVezbaId),
      dayOfWeek: REV_DAY_MAP[izabraniDan],
      sets: sets,
      reps: reps,
      startTime: "10:00:00", // Default
      completed: false
    };

    try {
      await api.createWorkoutExercise(request);
      await fetchData(); // Osveži podatke
      
      // Reset forme
      setIzabranaVezbaId("");
      setDetaljiVezbe("");
      setDodatnoTrajanje("0");
      setPrikaziFormu(false);
    } catch (error) {
      console.error("Error creating workout exercise:", error);
    }
  };

  // Funkcija za brisanje pojedinačne vežbe
  const obrisiVezbu = async (danId, vezbaId) => {
    try {
      await api.deleteWorkoutExercise(vezbaId);
      await fetchData();
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
            Nedeljni plan treninga
          </h2>
        </div>

        {/* Action Buttons */}
        <div className="flex gap-3 mb-6">
          <button
              onClick={() => { setPrikaziFormu(!prikaziFormu); setModZaUredjivanje(false); }}
              type="button"
              className={`px-5 py-2 text-sm rounded font-medium transition-colors cursor-pointer ${prikaziFormu ? 'bg-red-600 text-white' : 'bg-primary text-white hover:bg-primary/90'}`}
          >
            {prikaziFormu ? "Zatvori" : "+ Novi plan"}
          </button>
          <button
              onClick={() => { setModZaUredjivanje(!modZaUredjivanje); setPrikaziFormu(false); }}
              type="button"
              className={`px-5 py-2 text-sm rounded font-medium border transition-colors cursor-pointer ${modZaUredjivanje ? 'bg-amber-600 text-white border-transparent' : 'bg-secondary border-border text-foreground hover:bg-secondary/80'}`}
          >
            {modZaUredjivanje ? "Završi uređivanje" : "Uredi plan"}
          </button>
          <button type="button" className="bg-secondary border border-border text-foreground px-5 py-2 text-sm rounded hover:bg-secondary/80 transition-colors">
            Istorija
          </button>
        </div>

        {/* Režim obaveštenja za uređivanje */}
        {modZaUredjivanje && (
            <div className="bg-amber-500/10 border border-amber-500/30 text-amber-500 rounded-lg p-3 mb-6 text-sm flex justify-between items-center animate-pulse">
              <span>U režimu ste za uređivanje. Možete uklanjati vežbe sa crvenim "X" i menjati minutažu na dnu svakog dana.</span>
              <button type="button" onClick={() => setModZaUredjivanje(false)} className="underline text-xs font-bold cursor-pointer">Završi</button>
            </div>
        )}

        {/* Forma za unos vežbe */}
        {prikaziFormu && (
            <form onSubmit={handleDodajVezbu} className="bg-card border border-primary/40 rounded-lg p-4 mb-6 grid grid-cols-1 md:grid-cols-4 gap-3 items-end shadow-md">
              <div>
                <label className="block text-xs text-muted-foreground mb-1 font-medium">Izaberi dan</label>
                <select
                    value={izabraniDan}
                    onChange={(e) => setIzabraniDan(e.target.value)}
                    className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                >
                  {dani.map(dan => (
                      <option key={dan.id} value={dan.id}>{dan.ime}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs text-muted-foreground mb-1 font-medium">Naziv vežbe</label>
                <select
                    value={izabranaVezbaId}
                    onChange={(e) => setIzabranaVezbaId(e.target.value)}
                    className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                    required
                >
                  <option value="">Izaberi vežbu</option>
                  {sveVezbe.map(v => (
                      <option key={v.id} value={v.id}>{v.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs text-muted-foreground mb-1 font-medium">Serije / Ponavljanja</label>
                <input
                    type="text"
                    placeholder="npr. 3×10"
                    value={detaljiVezbe}
                    onChange={(e) => setDetaljiVezbe(e.target.value)}
                    className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                    required
                />
              </div>
              <div className="flex gap-2">
                <div className="w-1/2">
                  <label className="block text-xs text-muted-foreground mb-1 font-medium">Trajanje (min)</label>
                  <input
                      type="number"
                      value={dodatnoTrajanje}
                      onChange={(e) => setDodatnoTrajanje(e.target.value)}
                      className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                      min="0"
                  />
                </div>
                <button type="submit" className="w-1/2 bg-primary text-white text-sm font-medium rounded p-2 hover:bg-primary/90 transition-colors cursor-pointer">
                  Sačuvaj
                </button>
              </div>
            </form>
        )}

        {/* Week Overview Header */}
        <div className="bg-secondary border border-border rounded-lg p-4 mb-4">
          <div className="flex justify-between items-center">
            <div className="text-sm font-medium text-foreground">Nedelja: 22.03.2026 – 28.03.2026</div>
            <div className="flex gap-2">
              <button type="button" className="bg-card border border-border text-foreground px-3 py-1 text-sm rounded hover:bg-secondary transition-colors">
                ← Prethodna
              </button>
              <button type="button" className="bg-card border border-border text-foreground px-3 py-1 text-sm rounded hover:bg-secondary transition-colors">
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
                                <div className="pr-4">
                                  <div className={`text-xs font-medium ${vezba.completed ? 'text-emerald-500 line-through' : 'text-foreground'}`}>
                                    {vezba.naziv || 'Vežba'} {vezba.completed && '✓'}
                                  </div>
                                  <div className="text-xs text-primary font-semibold mt-0.5">{vezba.detalji}</div>
                                </div>

                                {/* OVDE JE BILA GREŠKA - ispravljeno na vezba.id */}
                                {modZaUredjivanje && (
                                    <button
                                        type="button"
                                        onClick={() => obrisiVezbu(dan.id, vezba.id)}
                                        className="text-red-500 hover:text-red-700 text-xs font-bold px-1 rounded bg-red-500/10 hover:bg-red-500/20 cursor-pointer transition-colors align-middle"
                                        title="Obriši vežbu"
                                    >
                                      ✕
                                    </button>
                                )}
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