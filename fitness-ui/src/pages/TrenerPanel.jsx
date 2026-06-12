import { useState, useEffect, useMemo } from 'react';
import { api } from '../services/api';
import { LoadingSpinner } from '../components/Spinner';
import { Modal } from '../components/Modal';
import { Input } from '../components/Input';
import { useToast } from '../contexts/ToastContext';
import { useAuth } from '../contexts/AuthContext';

const BARLOW = { fontFamily: "'Barlow Condensed', sans-serif" };
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
      id: `${yyyy}-${mm}-${dd}`,
      name: DAY_NAMES[d.getDay()],
      date: `${dd}.${mm}`,
      dateObj: d,
      exercises: []
    };
  });
}

const todayStr = (() => { 
  const n = new Date(); 
  const dd = String(n.getDate()).padStart(2,'0'); 
  const mm = String(n.getMonth()+1).padStart(2,'0'); 
  return `${n.getFullYear()}-${mm}-${dd}`; 
})();

export function TrenerPanel() {
  const { user } = useAuth();
  const [trainerClients, setTrainerClients] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [activeTab, setActiveTab] = useState('clients');
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({});
  const toast = useToast();
  const [saving, setSaving] = useState(false);

  // Client workout plan states
  const [selectedClient, setSelectedClient] = useState(null);
  const [clientFitnessGoal, setClientFitnessGoal] = useState(null);
  const [clientWeekOffset, setClientWeekOffset] = useState(0);
  const [clientDays, setClientDays] = useState([]);
  const [allExercises, setAllExercises] = useState([]);
  const [planLoading, setPlanLoading] = useState(false);
  const [showPlanForm, setShowPlanForm] = useState(false);
  const [planForm, setPlanForm] = useState({
    exerciseId: '',
    scheduledDate: todayStr,
    sets: '3',
    reps: '10',
    startTime: '10:00',
    restSec: '60'
  });

  useEffect(() => { fetchAll(); }, [user]);

  useEffect(() => {
    if (selectedClient) {
      fetchClientWorkouts();
      fetchClientFitnessGoal();
    }
  }, [selectedClient, clientWeekOffset]);

  const fetchClientFitnessGoal = async () => {
    if (!selectedClient) return;
    try {
      const goal = await api.getActiveFitnessGoal(selectedClient.clientId);
      setClientFitnessGoal(goal);
    } catch (error) {
      setClientFitnessGoal(null);
    }
  };

  const fetchAll = async () => {
    if (!user?.id) return;
    setLoading(true);
    try {
      const [tc, users, ex] = await Promise.all([
        api.getTrainerClientsByTrainerId(user.id),
        api.getUsers(),
        api.getExercises(0, 100)
      ]);
      setTrainerClients(tc || []);
      setAllUsers(users || []);
      setAllExercises(ex.content || []);
    } catch {
      toast('Greška pri učitavanju podataka. Provjerite konekciju i pokušajte ponovo.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const fetchClientWorkouts = async () => {
    if (!selectedClient) return;
    setPlanLoading(true);
    try {
      const nextWeek = clientWeekOffset > 0;
      const exercises = await api.getWorkoutExercises(selectedClient.clientId, nextWeek);
      
      const weekDays = buildWeekDays(clientWeekOffset);
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
            completed: ex.completed
          }));

        return { ...day, exercises: dayExercises };
      });
      setClientDays(newDays);
    } catch {
      toast('Greška pri učitavanju treninga klijenta.', 'error');
    } finally {
      setPlanLoading(false);
    }
  };

  const handleAddPlanExercise = async (e) => {
    e.preventDefault();
    if (!planForm.exerciseId || !planForm.scheduledDate || !selectedClient) return;

    const payload = {
      userId: selectedClient.clientId,
      exerciseId: parseInt(planForm.exerciseId),
      scheduledDate: planForm.scheduledDate,
      sets: parseInt(planForm.sets) || 3,
      reps: parseInt(planForm.reps) || 10,
      startTime: planForm.startTime ? planForm.startTime + ":00" : "10:00:00",
      restSec: parseInt(planForm.restSec) || 60,
      completed: false
    };

    setSaving(true);
    try {
      await api.createWorkoutExercise(payload);
      await fetchClientWorkouts();
      setShowPlanForm(false);
      toast('Vježba uspješno dodana klijentu.', 'success');
    } catch {
      toast('Greška pri dodavanju vježbe.', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDeletePlanExercise = async (exerciseId) => {
    if (!confirm('Obrisati vježbu iz klijentovog plana?')) return;
    try {
      await api.deleteWorkoutExercise(exerciseId);
      await fetchClientWorkouts();
      toast('Vježba obrisana.', 'success');
    } catch {
      toast('Greška pri brisanju vježbe.', 'error');
    }
  };

  const openModal = (type, data = null) => { setModal({ type, data }); setForm(data || {}); };
  const closeModal = () => { setModal(null); setForm({}); };

  const handleSave = async () => {
    setSaving(true);
    try {
      if (modal.type === 'client') {
        if (modal.data) await api.updateTrainerClient(modal.data.id, form);
        else await api.createTrainerClient(form);
      } else if (modal.type === 'notification') {
        if (modal.data) await api.updateNotification(modal.data.id, form);
        else await api.createNotification(form);
      }
      await fetchAll();
      closeModal();
      toast('Uspješno sačuvano.', 'success');
    } catch { toast('Greška pri čuvanju. Pokušajte ponovo.', 'error'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (type, id) => {
    if (!confirm('Obrisati?')) return;
    try {
      if (type === 'client') await api.deleteTrainerClient(id);
      else if (type === 'notification') await api.deleteNotification(id);
      await fetchAll();
    } catch { toast('Greška pri brisanju. Pokušajte ponovo.', 'error'); }
  };

  const f = (key) => (e) => setForm(prev => ({ ...prev, [key]: e.target.value }));

  const handleAddAsClient = async (clientId) => {
    setSaving(true);
    try {
      const payload = {
        trainerId: user.id,
        clientId: clientId,
        startDate: new Date().toISOString().split('T')[0],
        status: 'ACTIVE'
      };
      await api.createTrainerClient(payload);
      await fetchAll();
      toast('Klijent uspješno dodan.', 'success');
    } catch {
      toast('Greška pri dodavanju klijenta.', 'error');
    } finally {
      setSaving(false);
    }
  };

  const getClientName = (clientId) => {
    const u = allUsers.find(user => user.id === clientId);
    return u ? `${u.firstName} ${u.lastName}` : `Klijent #${clientId}`;
  };

  const clientIds = trainerClients.map(tc => tc.clientId);
  const nonClients = allUsers.filter(u => u.id !== user.id && !clientIds.includes(u.id) && u.roleName === 'USER');

  const TABS = [
    ['clients', 'Klijenti', trainerClients.length],
    ['users', 'Korisnici', nonClients.length],
  ];

  const handleTabChange = (key) => {
    setActiveTab(key);
    setSelectedClient(null);
  };

  return (
    <>
      <div className="mb-6 flex items-center gap-3">
        <div className="w-10 h-10 bg-primary rounded flex items-center justify-center flex-shrink-0">
          <span className="text-white text-lg font-bold" style={BARLOW}>FT</span>
        </div>
        <h2 className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3 flex-1" style={BARLOW}>
          Trener Panel
        </h2>
      </div>

      {/* Stats row */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        {TABS.map(([, label, count]) => (
          <div key={label} className="bg-card border border-border rounded-lg p-4 text-center">
            <div className="text-xs text-muted-foreground mb-1">{label}</div>
            <div className="text-2xl font-bold text-primary" style={BARLOW}>{count}</div>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 bg-secondary border border-border rounded-lg p-1 w-fit">
        {TABS.map(([key, label]) => (
          <button
            key={key}
            type="button"
            onClick={() => handleTabChange(key)}
            className={`px-4 py-2 text-sm rounded-md transition-colors ${
              activeTab === key ? 'bg-primary text-white font-medium' : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      {loading ? (
        <LoadingSpinner message="Učitavanje podataka..." size="lg" />
      ) : (
        <>
          {/* Clients */}
          {activeTab === 'clients' && !selectedClient && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{trainerClients.length} klijenata</span>
              </div>
              <div className="bg-card border border-border rounded-lg overflow-hidden">
                {trainerClients.length === 0 && (
                  <div className="p-8 text-center text-muted-foreground text-sm">Nema klijenata. Dodajte prvog!</div>
                )}
                {trainerClients.length > 0 && (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border bg-secondary">
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Klijent</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Datum od</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Status</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Akcije</th>
                      </tr>
                    </thead>
                    <tbody>
                      {trainerClients.map((tc, i) => (
                        <tr key={tc.id} className={`border-b border-border ${i % 2 === 0 ? 'bg-card' : 'bg-secondary/30'}`}>
                          <td className="px-4 py-3">
                            <div className="font-medium text-foreground">{getClientName(tc.clientId)}</div>
                            <div className="text-[10px] text-muted-foreground italic">ID: #{tc.clientId}</div>
                          </td>
                          <td className="px-4 py-3 text-muted-foreground">{tc.startDate || '—'}</td>
                          <td className="px-4 py-3">
                            <span className={`text-xs px-2 py-0.5 rounded ${tc.active !== false ? 'bg-emerald-500/20 text-emerald-400' : 'bg-muted text-muted-foreground'}`}>
                              {tc.active !== false ? 'Aktivan' : 'Neaktivan'}
                            </span>
                          </td>
                          <td className="px-4 py-3 text-right">
                            <div className="flex gap-2 justify-end">
                              <button 
                                type="button" 
                                onClick={() => setSelectedClient(tc)}
                                className="bg-primary/10 border border-primary/30 text-primary px-2 py-1 text-xs rounded hover:bg-primary/20 transition-colors"
                              >
                                Treninzi
                              </button>
                              <button type="button" onClick={() => handleDelete('client', tc.id)} className="bg-destructive/10 border border-destructive/30 text-destructive px-2 py-1 text-xs rounded hover:bg-destructive/20">Obriši</button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </>
          )}

          {/* Client Workout Plan Detail */}
          {activeTab === 'clients' && selectedClient && (
            <div className="animate-in fade-in duration-500">
              <div className="flex items-center justify-between mb-6 border-b border-border pb-4">
                <div className="flex items-center gap-4">
                  <button 
                    onClick={() => setSelectedClient(null)}
                    className="w-8 h-8 rounded-full bg-secondary flex items-center justify-center text-foreground hover:bg-border transition-colors"
                    title="Nazad na listu"
                  >
                    ←
                  </button>
                  <div>
                    <h3 className="text-xl font-bold text-foreground" style={BARLOW}>
                      Plan treninga: {getClientName(selectedClient.clientId)}
                    </h3>
                    <p className="text-xs text-muted-foreground italic">Prilagođavanje vježbi za klijenta</p>
                  </div>
                </div>
                <button
                  onClick={() => setShowPlanForm(!showPlanForm)}
                  className={`px-5 py-2 text-sm rounded font-medium transition-colors cursor-pointer flex-shrink-0 min-h-[44px] ${showPlanForm ? 'bg-red-600 text-white' : 'bg-primary text-white hover:bg-primary/90'}`}
                >
                  {showPlanForm ? 'Zatvori' : '+ Nova vježba'}
                </button>
              </div>

              {showPlanForm && (
                <form onSubmit={handleAddPlanExercise} className="bg-card border border-primary/40 rounded-lg p-4 mb-6 shadow-md animate-in slide-in-from-top duration-300">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-3">
                    <div>
                      <label className="block text-xs text-muted-foreground mb-1 font-medium">Datum</label>
                      <input
                        type="date"
                        value={planForm.scheduledDate}
                        onChange={(e) => setPlanForm(prev => ({ ...prev, scheduledDate: e.target.value }))}
                        className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-muted-foreground mb-1 font-medium">Vježba</label>
                      <select
                        value={planForm.exerciseId}
                        onChange={(e) => setPlanForm(prev => ({ ...prev, exerciseId: e.target.value }))}
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
                      <label className="block text-xs text-muted-foreground mb-1 font-medium">Početak</label>
                      <input
                        type="time"
                        value={planForm.startTime}
                        onChange={(e) => setPlanForm(prev => ({ ...prev, startTime: e.target.value }))}
                        className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                      />
                    </div>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
                    <div>
                      <label className="block text-xs text-muted-foreground mb-1 font-medium">Serije</label>
                      <input
                        type="number"
                        value={planForm.sets}
                        onChange={(e) => setPlanForm(prev => ({ ...prev, sets: e.target.value }))}
                        className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                        min="1"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-muted-foreground mb-1 font-medium">Ponavljanja</label>
                      <input
                        type="number"
                        value={planForm.reps}
                        onChange={(e) => setPlanForm(prev => ({ ...prev, reps: e.target.value }))}
                        className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                        min="1"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-muted-foreground mb-1 font-medium">Odmor (s)</label>
                      <input
                        type="number"
                        value={planForm.restSec}
                        onChange={(e) => setPlanForm(prev => ({ ...prev, restSec: e.target.value }))}
                        className="w-full bg-secondary border border-border rounded p-2 text-sm text-foreground focus:outline-none focus:border-primary"
                        min="0"
                      />
                    </div>
                    <button type="submit" disabled={saving} className="bg-primary text-white text-sm font-medium rounded p-2 hover:bg-primary/90 transition-colors disabled:opacity-50 min-h-[44px]">
                      {saving ? 'Čuvanje...' : 'Sačuvaj'}
                    </button>
                  </div>
                </form>
              )}

              {/* Fitness Goal Card */}
              <div className="bg-card border border-border rounded-lg p-4 mb-6 shadow-sm">
                <h4 className="text-sm font-bold uppercase mb-3 text-primary" style={BARLOW}>Fitnes ciljevi klijenta</h4>
                {clientFitnessGoal ? (
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div className="bg-secondary/50 p-2 rounded">
                      <div className="text-[10px] text-muted-foreground uppercase font-bold">Cilj</div>
                      <div className="text-sm font-medium text-foreground">{clientFitnessGoal.goalType}</div>
                    </div>
                    <div className="bg-secondary/50 p-2 rounded">
                      <div className="text-[10px] text-muted-foreground uppercase font-bold">Ciljna težina</div>
                      <div className="text-sm font-medium text-foreground">{clientFitnessGoal.targetValue} kg</div>
                    </div>
                    <div className="bg-secondary/50 p-2 rounded">
                      <div className="text-[10px] text-muted-foreground uppercase font-bold">Datum do</div>
                      <div className="text-sm font-medium text-foreground">{clientFitnessGoal.deadline}</div>
                    </div>
                  </div>
                ) : (
                  <div className="text-sm text-muted-foreground italic bg-secondary/30 p-4 rounded text-center border border-dashed border-border">
                    Korisnik nije postavio fitnes cilj.
                  </div>
                )}
              </div>

              <div className="bg-secondary border border-border rounded-lg p-3 mb-4 flex justify-between items-center">
                <div className="text-sm font-medium text-foreground">
                  {clientDays.length > 0 && `Sedmica: ${clientDays[0].date} – ${clientDays[6].date}`}
                  <span className="ml-2 text-[10px] font-bold text-primary">
                    {clientWeekOffset === 0 ? '(Tekuća)' : '(Iduća)'}
                  </span>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => setClientWeekOffset(0)}
                    disabled={clientWeekOffset === 0}
                    className="px-3 py-1 text-xs bg-card border border-border rounded disabled:opacity-40"
                  >
                    ← Prethodna
                  </button>
                  <button
                    onClick={() => setClientWeekOffset(1)}
                    disabled={clientWeekOffset === 1}
                    className="px-3 py-1 text-xs bg-card border border-border rounded disabled:opacity-40"
                  >
                    Sljedeća →
                  </button>
                </div>
              </div>

              {planLoading ? (
                <div className="py-20 flex justify-center"><LoadingSpinner size="md" /></div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-7 gap-2">
                  {clientDays.map(day => {
                    const hasExercises = day.exercises.length > 0;
                    return (
                      <div key={day.id} className="bg-card border border-border rounded-lg overflow-hidden flex flex-col min-h-[200px]">
                        <div className={`p-2 text-center border-b border-border ${hasExercises ? "bg-primary text-white" : "bg-secondary text-foreground"}`}>
                          <div className="text-[10px] font-bold uppercase">{day.name}</div>
                          <div className={`text-xs font-bold ${hasExercises ? "text-white/80" : "text-muted-foreground"}`}>{day.date}</div>
                        </div>
                        <div className="p-2 space-y-1.5 flex-1">
                          {hasExercises ? (
                            day.exercises.map(ex => (
                              <div key={ex.id} className="bg-secondary rounded p-1.5 relative group flex flex-col">
                                <div className="text-[10px] text-muted-foreground mb-0.5">{ex.startTime || '--:--'}</div>
                                <div className={`text-xs font-medium leading-tight mb-0.5 ${ex.completed ? 'text-emerald-500 line-through' : 'text-foreground'}`}>
                                  {ex.name}
                                </div>
                                <div className="text-[10px] text-primary font-bold">{ex.details}</div>
                                {ex.completed && (
                                  <div className="absolute top-1 right-1 text-emerald-500 text-[10px]" title="Završeno">✓</div>
                                )}
                                <button 
                                  onClick={() => handleDeletePlanExercise(ex.id)}
                                  className="absolute -top-1 -right-1 w-4 h-4 bg-destructive text-white rounded-full text-[8px] flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity z-10"
                                  title="Obriši"
                                >
                                  ✕
                                </button>
                              </div>
                            ))
                          ) : (
                            <div className="h-full flex items-center justify-center italic text-[10px] text-muted-foreground opacity-50">
                              Bez vježbi
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

          {/* Notifications */}
          {activeTab === 'notifications' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{notifications.length} obavijesti</span>
                <button type="button" onClick={() => openModal('notification')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  + Pošalji obavijest
                </button>
              </div>
              <div className="space-y-3">
                {notifications.length === 0 && (
                  <div className="bg-card border border-border rounded-lg p-8 text-center text-muted-foreground text-sm">Nema obavijesti.</div>
                )}
                {notifications.map((n) => (
                  <div key={n.id} className="bg-card border border-border rounded-lg p-4 flex items-start justify-between gap-4">
                    <div className="flex-1">
                      <div className="font-medium text-foreground text-sm mb-1" style={BARLOW}>{n.title || n.subject || `Obavijest #${n.id}`}</div>
                      <p className="text-xs text-muted-foreground">{n.message || n.content || '—'}</p>
                      <div className="text-xs text-muted-foreground mt-1">
                        {n.userId && <span>Korisnik: #{n.userId}</span>}
                        {n.createdAt && <span className="ml-3">{n.createdAt}</span>}
                      </div>
                    </div>
                    <div className="flex gap-2 flex-shrink-0">
                      <button type="button" onClick={() => openModal('notification', n)} className="bg-secondary border border-border text-foreground px-3 py-1.5 text-xs rounded hover:bg-secondary/80">Uredi</button>
                      <button type="button" onClick={() => handleDelete('notification', n.id)} className="bg-destructive/10 border border-destructive/30 text-destructive px-3 py-1.5 text-xs rounded hover:bg-destructive/20">Obriši</button>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}

          {/* All Users (Non-clients) */}
          {activeTab === 'users' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{nonClients.length} dostupnih korisnika</span>
              </div>
              <div className="bg-card border border-border rounded-lg overflow-hidden">
                {nonClients.length === 0 && (
                  <div className="p-8 text-center text-muted-foreground text-sm">Nema korisnika za prikaz.</div>
                )}
                {nonClients.length > 0 && (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border bg-secondary">
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">ID</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Ime i prezime</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Email</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Akcije</th>
                      </tr>
                    </thead>
                    <tbody>
                      {nonClients.map((u, i) => (
                        <tr key={u.id} className={`border-b border-border ${i % 2 === 0 ? 'bg-card' : 'bg-secondary/30'}`}>
                          <td className="px-4 py-3 text-muted-foreground">#{u.id}</td>
                          <td className="px-4 py-3 text-foreground font-medium">{u.firstName} {u.lastName}</td>
                          <td className="px-4 py-3 text-muted-foreground">{u.email}</td>
                          <td className="px-4 py-3 text-right">
                            <button
                              type="button"
                              onClick={() => handleAddAsClient(u.id)}
                              disabled={saving}
                              className="bg-primary text-white px-3 py-1.5 text-xs rounded font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                            >
                              + Dodaj klijenta
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </>
          )}
        </>
      )}

      {/* Modal */}
      {modal && (
        <Modal
          title={
            modal.type === 'client' ? (modal.data ? 'Uredi klijenta' : 'Dodaj klijenta') :
            (modal.data ? 'Uredi obavijest' : 'Nova obavijest')
          }
          onClose={closeModal}
        >
          <div className="space-y-4">
            {modal.type === 'client' && (
              <>
                <Input label="ID trenera" type="number" value={form.trainerId || ''} onChange={f('trainerId')} placeholder="ID trenera" />
                <Input label="ID klijenta" type="number" value={form.clientId || ''} onChange={f('clientId')} placeholder="ID klijenta" />
                <Input label="Datum početka" type="date" value={form.startDate || ''} onChange={f('startDate')} />
              </>
            )}

            {modal.type === 'notification' && (
              <>
                <Input label="Naslov" value={form.title || form.subject || ''} onChange={f('title')} placeholder="Naslov obavijesti" />
                <div>
                  <label className="block text-xs text-muted-foreground mb-1">Poruka</label>
                  <textarea
                    className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary resize-none"
                    rows={3}
                    value={form.message || form.content || ''}
                    onChange={(e) => setForm(prev => ({ ...prev, message: e.target.value }))}
                    placeholder="Sadržaj obavijesti..."
                  />
                </div>
                <Input label="ID korisnika" type="number" value={form.userId || ''} onChange={f('userId')} placeholder="ID korisnika" />
              </>
            )}

            <div className="flex gap-3 pt-2">
              <button type="button" onClick={handleSave} disabled={saving} className="flex-1 bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors disabled:opacity-50">
                {saving ? 'Čuvanje...' : 'Sačuvaj'}
              </button>
              <button type="button" onClick={closeModal} className="flex-1 bg-secondary border border-border text-foreground px-4 py-2 text-sm rounded hover:bg-secondary/80 transition-colors">
                Odustani
              </button>
            </div>
          </div>
        </Modal>
      )}
    </>
  );
}
