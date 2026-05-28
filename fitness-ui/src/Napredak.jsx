import { useState, useEffect } from 'react';
import { api } from './services/api';

const BARLOW = { fontFamily: "'Barlow Condensed', sans-serif" };

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
      <div className="bg-card border border-border rounded-lg w-full max-w-lg">
        <div className="flex items-center justify-between p-5 border-b border-border">
          <h3 className="text-base font-semibold text-foreground" style={BARLOW}>{title}</h3>
          <button type="button" onClick={onClose} className="text-muted-foreground hover:text-foreground text-xl leading-none">×</button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  );
}

function Input({ ...props }) {
  return (
    <input
      className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
      {...props}
    />
  );
}

function StatCard({ label, value, unit, color = 'text-primary' }) {
  return (
    <div className="bg-card border border-border rounded-lg p-4 text-center border-t-2" style={{ borderTopColor: 'var(--primary)' }}>
      <div className="text-xs text-muted-foreground mb-2">{label}</div>
      <div className={`text-3xl font-bold ${color}`} style={BARLOW}>{value ?? '—'}</div>
      {unit && <div className="text-xs text-muted-foreground mt-1">{unit}</div>}
    </div>
  );
}

export function Napredak() {
  const [entries, setEntries] = useState([]);
  const [fitnessGoals, setFitnessGoals] = useState([]);
  const [completedWorkouts, setCompletedWorkouts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('progress');
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { fetchAll(); }, []);

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [prog, goals, cw] = await Promise.all([
        api.getProgressEntries(),
        api.getFitnessGoals(),
        api.getCompletedWorkouts(),
      ]);
      setEntries(prog || []);
      setFitnessGoals(goals || []);
      setCompletedWorkouts(cw || []);
    } catch {
      setError('Greška pri učitavanju podataka');
    } finally {
      setLoading(false);
    }
  };

  const openModal = (type, data = null) => { setModal({ type, data }); setForm(data || {}); setError(''); };
  const closeModal = () => { setModal(null); setForm({}); setError(''); };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    try {
      if (modal.type === 'progress') {
        if (modal.data) await api.updateProgressEntry(modal.data.id, form);
        else await api.createProgressEntry(form);
      } else if (modal.type === 'goal') {
        if (modal.data) await api.updateFitnessGoal(modal.data.id, form);
        else await api.createFitnessGoal(form);
      } else if (modal.type === 'workout') {
        if (modal.data) await api.updateCompletedWorkout(modal.data.id, form);
        else await api.createCompletedWorkout(form);
      }
      await fetchAll();
      closeModal();
    } catch { setError('Greška pri čuvanju'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (type, id) => {
    if (!confirm('Obrisati unos?')) return;
    try {
      if (type === 'progress') await api.deleteProgressEntry(id);
      else if (type === 'goal') await api.deleteFitnessGoal(id);
      else if (type === 'workout') await api.deleteCompletedWorkout(id);
      await fetchAll();
    } catch { alert('Greška pri brisanju'); }
  };

  const f = (key) => (e) => setForm(prev => ({ ...prev, [key]: e.target.value }));

  const latestEntry = entries.length > 0 ? [...entries].sort((a, b) => new Date(b.date || 0) - new Date(a.date || 0))[0] : null;

  return (
    <>
      <div className="mb-6">
        <h2 className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3" style={BARLOW}>
          Napredak
        </h2>
      </div>

      {/* Summary stats */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        <StatCard label="Unosa napretka" value={entries.length} />
        <StatCard label="Posljednja težina" value={latestEntry?.weight} unit="kg" color="text-emerald-400" />
        <StatCard label="Ciljevi" value={fitnessGoals.length} color="text-blue-400" />
        <StatCard label="Završenih treninga" value={completedWorkouts.length} color="text-yellow-400" />
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 bg-secondary border border-border rounded-lg p-1 w-fit">
        {[['progress', 'Unosi napretka'], ['goals', 'Ciljevi'], ['workouts', 'Završeni treninzi']].map(([key, label]) => (
          <button
            key={key}
            type="button"
            onClick={() => setActiveTab(key)}
            className={`px-4 py-2 text-sm rounded-md transition-colors ${
              activeTab === key ? 'bg-primary text-white font-medium' : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-40 text-muted-foreground">Učitavanje...</div>
      ) : (
        <>
          {/* Progress Entries */}
          {activeTab === 'progress' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{entries.length} unosa</span>
                <button type="button" onClick={() => openModal('progress')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  + Novi unos
                </button>
              </div>
              <div className="bg-card border border-border rounded-lg overflow-hidden">
                {entries.length === 0 && (
                  <div className="p-8 text-center text-muted-foreground text-sm">Nema unosa napretka. Dodajte prvi!</div>
                )}
                {entries.length > 0 && (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border bg-secondary">
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Datum</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Težina (kg)</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">BMI</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Masti (%)</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Napomena</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Akcije</th>
                      </tr>
                    </thead>
                    <tbody>
                      {entries.map((e, i) => (
                        <tr key={e.id} className={`border-b border-border ${i % 2 === 0 ? 'bg-card' : 'bg-secondary/30'}`}>
                          <td className="px-4 py-3 text-foreground">{e.date || '—'}</td>
                          <td className="px-4 py-3 text-right text-primary font-semibold">{e.weight ?? '—'}</td>
                          <td className="px-4 py-3 text-right text-muted-foreground">{e.bmi ?? '—'}</td>
                          <td className="px-4 py-3 text-right text-muted-foreground">{e.bodyFatPercentage != null ? `${e.bodyFatPercentage}%` : '—'}</td>
                          <td className="px-4 py-3 text-muted-foreground text-xs">{e.notes || '—'}</td>
                          <td className="px-4 py-3 text-right">
                            <div className="flex gap-2 justify-end">
                              <button type="button" onClick={() => openModal('progress', e)} className="bg-secondary border border-border text-foreground px-2 py-1 text-xs rounded hover:bg-secondary/80">Uredi</button>
                              <button type="button" onClick={() => handleDelete('progress', e.id)} className="bg-destructive/10 border border-destructive/30 text-destructive px-2 py-1 text-xs rounded hover:bg-destructive/20">Obriši</button>
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

          {/* Fitness Goals */}
          {activeTab === 'goals' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{fitnessGoals.length} ciljeva</span>
                <button type="button" onClick={() => openModal('goal')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  + Novi cilj
                </button>
              </div>
              <div className="grid grid-cols-2 gap-4">
                {fitnessGoals.length === 0 && (
                  <div className="col-span-2 bg-card border border-border rounded-lg p-8 text-center text-muted-foreground text-sm">
                    Nema postavljenih ciljeva. Dodajte prvi!
                  </div>
                )}
                {fitnessGoals.map((g) => (
                  <div key={g.id} className="bg-card border border-border rounded-lg p-4">
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <div className="font-semibold text-foreground" style={BARLOW}>{g.goalType || g.type || `Cilj #${g.id}`}</div>
                        <div className="text-xs text-muted-foreground mt-1">
                          {g.startDate && <span>Od: {g.startDate}</span>}
                          {g.targetDate && <span className="ml-3">Do: {g.targetDate}</span>}
                        </div>
                      </div>
                      <span className={`text-xs px-2 py-1 rounded font-medium ${g.achieved ? 'bg-emerald-500/20 text-emerald-400' : 'bg-primary/10 text-primary'}`}>
                        {g.achieved ? 'Ostvareno' : 'U toku'}
                      </span>
                    </div>
                    {g.description && <p className="text-xs text-muted-foreground mb-3">{g.description}</p>}
                    <div className="flex gap-2">
                      <button type="button" onClick={() => openModal('goal', g)} className="bg-secondary border border-border text-foreground px-3 py-1.5 text-xs rounded hover:bg-secondary/80">Uredi</button>
                      <button type="button" onClick={() => handleDelete('goal', g.id)} className="bg-destructive/10 border border-destructive/30 text-destructive px-3 py-1.5 text-xs rounded hover:bg-destructive/20">Obriši</button>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}

          {/* Completed Workouts */}
          {activeTab === 'workouts' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{completedWorkouts.length} završenih treninga</span>
                <button type="button" onClick={() => openModal('workout')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  + Evidentiraj trening
                </button>
              </div>
              <div className="space-y-3">
                {completedWorkouts.length === 0 && (
                  <div className="bg-card border border-border rounded-lg p-8 text-center text-muted-foreground text-sm">
                    Nema evidentiranih treninga.
                  </div>
                )}
                {completedWorkouts.map((cw) => (
                  <div key={cw.id} className="bg-card border border-border rounded-lg p-4 flex items-center justify-between">
                    <div>
                      <div className="font-medium text-foreground text-sm" style={BARLOW}>{cw.workoutPlanId ? `Plan #${cw.workoutPlanId}` : `Trening #${cw.id}`}</div>
                      <div className="text-xs text-muted-foreground mt-1">
                        {cw.date && <span>{cw.date}</span>}
                        {cw.durationMinutes != null && <span className="ml-3">⏱ {cw.durationMinutes} min</span>}
                        {cw.notes && <span className="ml-3">{cw.notes}</span>}
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <button type="button" onClick={() => openModal('workout', cw)} className="bg-secondary border border-border text-foreground px-3 py-1.5 text-xs rounded hover:bg-secondary/80">Uredi</button>
                      <button type="button" onClick={() => handleDelete('workout', cw.id)} className="bg-destructive/10 border border-destructive/30 text-destructive px-3 py-1.5 text-xs rounded hover:bg-destructive/20">Obriši</button>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </>
      )}

      {/* Modal */}
      {modal && (
        <Modal
          title={
            modal.type === 'progress' ? (modal.data ? 'Uredi unos napretka' : 'Novi unos napretka') :
            modal.type === 'goal' ? (modal.data ? 'Uredi cilj' : 'Novi cilj') :
            (modal.data ? 'Uredi trening' : 'Evidentiraj trening')
          }
          onClose={closeModal}
        >
          <div className="space-y-4">
            {error && <div className="bg-destructive/10 border border-destructive text-destructive px-3 py-2 rounded text-sm">{error}</div>}

            {modal.type === 'progress' && (
              <>
                <div><label className="block text-xs text-muted-foreground mb-1">Datum</label><Input type="date" value={form.date || ''} onChange={f('date')} /></div>
                <div className="grid grid-cols-2 gap-3">
                  <div><label className="block text-xs text-muted-foreground mb-1">Težina (kg)</label><Input type="number" step="0.1" value={form.weight || ''} onChange={f('weight')} placeholder="75.5" /></div>
                  <div><label className="block text-xs text-muted-foreground mb-1">BMI</label><Input type="number" step="0.1" value={form.bmi || ''} onChange={f('bmi')} placeholder="22.5" /></div>
                  <div><label className="block text-xs text-muted-foreground mb-1">Masti (%)</label><Input type="number" step="0.1" value={form.bodyFatPercentage || ''} onChange={f('bodyFatPercentage')} placeholder="18.0" /></div>
                  <div><label className="block text-xs text-muted-foreground mb-1">Mišićna masa (kg)</label><Input type="number" step="0.1" value={form.muscleMass || ''} onChange={f('muscleMass')} placeholder="60.0" /></div>
                </div>
                <div><label className="block text-xs text-muted-foreground mb-1">Napomena</label><Input value={form.notes || ''} onChange={f('notes')} placeholder="Napomena..." /></div>
              </>
            )}

            {modal.type === 'goal' && (
              <>
                <div><label className="block text-xs text-muted-foreground mb-1">Tip cilja</label><Input value={form.goalType || form.type || ''} onChange={f('goalType')} placeholder="npr. Gubitak težine" /></div>
                <div><label className="block text-xs text-muted-foreground mb-1">Opis</label><Input value={form.description || ''} onChange={f('description')} placeholder="Opisite vaš cilj..." /></div>
                <div className="grid grid-cols-2 gap-3">
                  <div><label className="block text-xs text-muted-foreground mb-1">Početni datum</label><Input type="date" value={form.startDate || ''} onChange={f('startDate')} /></div>
                  <div><label className="block text-xs text-muted-foreground mb-1">Ciljni datum</label><Input type="date" value={form.targetDate || ''} onChange={f('targetDate')} /></div>
                </div>
                <div><label className="block text-xs text-muted-foreground mb-1">Ciljana vrijednost</label><Input type="number" step="0.1" value={form.targetValue || ''} onChange={f('targetValue')} placeholder="npr. 70 (kg)" /></div>
              </>
            )}

            {modal.type === 'workout' && (
              <>
                <div><label className="block text-xs text-muted-foreground mb-1">ID plana treninga</label><Input type="number" value={form.workoutPlanId || ''} onChange={f('workoutPlanId')} placeholder="ID plana" /></div>
                <div><label className="block text-xs text-muted-foreground mb-1">Datum</label><Input type="date" value={form.date || ''} onChange={f('date')} /></div>
                <div><label className="block text-xs text-muted-foreground mb-1">Trajanje (min)</label><Input type="number" value={form.durationMinutes || ''} onChange={f('durationMinutes')} placeholder="45" /></div>
                <div><label className="block text-xs text-muted-foreground mb-1">Napomena</label><Input value={form.notes || ''} onChange={f('notes')} placeholder="Bilješke o treningu..." /></div>
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
