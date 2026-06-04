import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { api } from '../services/api';

const BARLOW = { fontFamily: "'Barlow Condensed', sans-serif" };

function Input({ label, ...props }) {
  return (
    <div>
      <label className="block text-xs text-muted-foreground mb-1">{label}</label>
      <input
        className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
        {...props}
      />
    </div>
  );
}

const GOAL_TYPES = [
  { value: 'WEIGHT_LOSS', label: 'Mršanje' },
  { value: 'MUSCLE_GAIN', label: 'Izgradnja mišića' },
  { value: 'MAINTENANCE', label: 'Održavanje forme' },
  { value: 'ENDURANCE', label: 'Poboljšanje kondicije' },
];

const TIME_FRAMES = [
  { value: 3, label: '3 meseca' },
  { value: 6, label: '6 meseci' },
  { value: 9, label: '9 meseci' },
  { value: 12, label: '12 meseci' },
];

export function Profil() {
  const { user, logout, refreshUser } = useAuth();
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({
    username: user?.username || '',
    email: user?.email || '',
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    age: user?.age || '',
    height: user?.height || '',
    weight: user?.weight || '',
    gender: user?.gender || '',
  });
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  
  const [activeGoal, setActiveGoal] = useState(null);
  const [goalForm, setGoalForm] = useState({
    goalType: '',
    targetValue: '',
    timeFrame: 6,
  });
  const [editingGoal, setEditingGoal] = useState(false);
  const [savingGoal, setSavingGoal] = useState(false);
  const [goalSuccess, setGoalSuccess] = useState('');
  const [goalError, setGoalError] = useState('');

  const f = (key) => (e) => setForm(prev => ({ ...prev, [key]: e.target.value }));
  const fg = (key) => (e) => setGoalForm(prev => ({ ...prev, [key]: e.target.value }));
  
  useEffect(() => {
    if (user?.id) {
      loadActiveGoal();
    }
  }, [user?.id]);
  
  const loadActiveGoal = async () => {
    try {
      const goal = await api.getActiveFitnessGoal(user.id);
      if (goal) {
        setActiveGoal(goal);
        setGoalForm({
          goalType: goal.goalType,
          targetValue: goal.targetValue || '',
          timeFrame: goal.deadline ? calculateMonthsDiff(new Date(), new Date(goal.deadline)) : 6,
        });
      } else {
        setActiveGoal(null);
      }
    } catch (err) {
      setActiveGoal(null);
    }
  };
  
  const calculateMonthsDiff = (start, end) => {
    const months = (end.getFullYear() - start.getFullYear()) * 12 + (end.getMonth() - start.getMonth());
    return Math.max(3, Math.min(12, months));
  };
  
  const getDeadlineFromMonths = (months) => {
    const date = new Date();
    date.setMonth(date.getMonth() + months);
    return date.toISOString().split('T')[0];
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      if (user?.id) {
        const profileData = {
          firstName: form.firstName,
          lastName: form.lastName,
          age: form.age ? parseInt(form.age) : null,
          height: form.height ? parseInt(form.height) : null,
          weight: form.weight ? parseInt(form.weight) : null,
          gender: form.gender,
        };
        await api.updateUserProfile(user.id, profileData);
        await refreshUser();
        setSuccess('Profil uspješno ažuriran.');
      }
      setEditing(false);
    } catch {
      setError('Greška pri čuvanju profila.');
    } finally {
      setSaving(false);
    }
  };
  
  const handleSaveGoal = async () => {
    setSavingGoal(true);
    setGoalError('');
    setGoalSuccess('');
    try {
      const goalData = {
        userId: user.id,
        goalType: goalForm.goalType,
        targetValue: goalForm.targetValue ? parseFloat(goalForm.targetValue) : null,
        isActive: true,
        deadline: getDeadlineFromMonths(parseInt(goalForm.timeFrame)),
      };
      
      if (activeGoal) {
        // Deaktiviraj postojeći cilj - zadrži njegove originalne podatke
        await api.updateFitnessGoal(activeGoal.id, { 
          userId: activeGoal.userId,
          goalType: activeGoal.goalType,
          targetValue: activeGoal.targetValue,
          isActive: false,
          deadline: activeGoal.deadline,
        });
      }
      
      // Kreiraj novi aktivan cilj
      const newGoal = await api.createFitnessGoal(goalData);
      setActiveGoal(newGoal);
      setGoalSuccess('Cilj uspješno sačuvan.');
      setEditingGoal(false);
      await loadActiveGoal();
    } catch (err) {
      setGoalError('Greška pri čuvanju cilja.');
      console.error('Error saving goal:', err);
    } finally {
      setSavingGoal(false);
    }
  };

  const initials = (user?.username || 'K').substring(0, 2).toUpperCase();

  return (
    <>
      <div className="mb-6">
        <h2 className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3" style={BARLOW}>
          Profil
        </h2>
      </div>

      <div className="max-w-2xl w-full space-y-6">
        {/* Avatar & Name card */}
        <div className="bg-card border border-border rounded-lg p-6 flex items-center gap-5">
          <div className="w-20 h-20 rounded-full bg-primary/20 border-2 border-primary/40 flex items-center justify-center flex-shrink-0">
            <span className="text-primary text-2xl font-bold" style={BARLOW}>{initials}</span>
          </div>
          <div>
            <div className="text-xl font-bold text-foreground" style={BARLOW}>{user?.username || 'Korisnik'}</div>
            <div className="text-sm text-muted-foreground mt-0.5">{user?.email || ''}</div>
            {user?.roles?.length > 0 && (
              <div className="flex gap-2 mt-2">
                {user.roles.map((r, i) => (
                  <span key={i} className="text-xs bg-primary/10 text-primary px-2 py-0.5 rounded">{r}</span>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Info card */}
        <div className="bg-card border border-border rounded-lg p-6">
          <div className="flex items-center justify-between mb-5">
            <h3 className="text-base font-semibold text-foreground" style={BARLOW}>Informacije o nalogu</h3>
            {!editing && (
              <button
                type="button"
                onClick={() => { setEditing(true); setSuccess(''); setError(''); }}
                className="bg-secondary border border-border text-foreground px-4 py-1.5 text-sm rounded hover:bg-secondary/80 transition-colors"
              >
                Uredi profil
              </button>
            )}
          </div>

          {success && <div className="bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 px-3 py-2 rounded text-sm mb-4">{success}</div>}
          {error && <div className="bg-destructive/10 border border-destructive text-destructive px-3 py-2 rounded text-sm mb-4">{error}</div>}

          {editing ? (
            <div className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input label="Ime" value={form.firstName} onChange={f('firstName')} placeholder="Ime" />
                <Input label="Prezime" value={form.lastName} onChange={f('lastName')} placeholder="Prezime" />
              </div>
              <Input label="Email adresa" type="email" value={form.email} onChange={f('email')} placeholder="email@example.com" />
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input label="Godine" type="number" value={form.age} onChange={f('age')} placeholder="Godine" />
                <Input label="Visina (cm)" type="number" value={form.height} onChange={f('height')} placeholder="Visina (cm)" />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input label="Težina (kg)" type="number" value={form.weight} onChange={f('weight')} placeholder="Težina (kg)" />
                <div>
                  <label className="block text-xs text-muted-foreground mb-1">Pol</label>
                  <select
                    value={form.gender}
                    onChange={f('gender')}
                    className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  >
                    <option value="">Izaberite pol</option>
                    <option value="MALE">Muški</option>
                    <option value="FEMALE">Ženski</option>
                    <option value="OTHER">Ostalo</option>
                  </select>
                </div>
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={handleSave}
                  disabled={saving}
                  className="bg-primary text-white px-5 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                >
                  {saving ? 'Čuvanje...' : 'Sačuvaj'}
                </button>
                <button
                  type="button"
                  onClick={() => setEditing(false)}
                  className="bg-secondary border border-border text-foreground px-5 py-2 text-sm rounded hover:bg-secondary/80 transition-colors"
                >
                  Odustani
                </button>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              {[
                ['Ime i prezime', `${user?.firstName || ''} ${user?.lastName || ''}`.trim() || '—'],
                ['Email adresa', user?.email || '—'],
                ['Godine', user?.age || '—'],
                ['Visina (cm)', user?.height || '—'],
                ['Težina (kg)', user?.weight || '—'],
                ['Pol', user?.gender === 'MALE' ? 'Muški' : user?.gender === 'FEMALE' ? 'Ženski' : user?.gender || '—'],
              ].map(([label, value]) => (
                <div key={label} className="flex items-center border-b border-border pb-3 last:border-0 last:pb-0">
                  <span className="text-sm text-muted-foreground w-40">{label}</span>
                  <span className="text-sm text-foreground">{value}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Fitness Ciljevi */}
        <div className="bg-card border border-border rounded-lg p-6">
          <div className="flex items-center justify-between mb-5">
            <h3 className="text-base font-semibold text-foreground uppercase" style={BARLOW}>Fitness Ciljevi</h3>
            {!editingGoal && (
              <button
                type="button"
                onClick={() => { setEditingGoal(true); setGoalSuccess(''); setGoalError(''); }}
                className="bg-secondary border border-border text-foreground px-4 py-1.5 text-sm rounded hover:bg-secondary/80 transition-colors"
              >
                {activeGoal ? 'Promijeni cilj' : 'Postavi cilj'}
              </button>
            )}
          </div>

          {goalSuccess && <div className="bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 px-3 py-2 rounded text-sm mb-4">{goalSuccess}</div>}
          {goalError && <div className="bg-destructive/10 border border-destructive text-destructive px-3 py-2 rounded text-sm mb-4">{goalError}</div>}

          {editingGoal ? (
            <div className="space-y-4">
              <div>
                <label className="block text-xs text-muted-foreground mb-1">Primarni cilj</label>
                <select
                  value={goalForm.goalType}
                  onChange={fg('goalType')}
                  className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                >
                  <option value="">Izaberite cilj</option>
                  {GOAL_TYPES.map(gt => (
                    <option key={gt.value} value={gt.value}>{gt.label}</option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="Ciljna težina (kg)"
                  type="number"
                  value={goalForm.targetValue}
                  onChange={fg('targetValue')}
                  placeholder="80"
                />
                <div>
                  <label className="block text-xs text-muted-foreground mb-1">Vremenski okvir</label>
                  <select
                    value={goalForm.timeFrame}
                    onChange={fg('timeFrame')}
                    className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  >
                    {TIME_FRAMES.map(tf => (
                      <option key={tf.value} value={tf.value}>{tf.label}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={handleSaveGoal}
                  disabled={savingGoal || !goalForm.goalType}
                  className="bg-primary text-white px-5 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                >
                  {savingGoal ? 'Čuvanje...' : 'Sačuvaj cilj'}
                </button>
                <button
                  type="button"
                  onClick={() => setEditingGoal(false)}
                  className="bg-secondary border border-border text-foreground px-5 py-2 text-sm rounded hover:bg-secondary/80 transition-colors"
                >
                  Odustani
                </button>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              {activeGoal ? (
                <>
                  <div className="flex items-center border-b border-border pb-3">
                    <span className="text-sm text-muted-foreground w-40">Primarni cilj</span>
                    <span className="text-sm text-foreground">
                      {GOAL_TYPES.find(gt => gt.value === activeGoal.goalType)?.label || activeGoal.goalType}
                    </span>
                  </div>
                  {activeGoal.targetValue && (
                    <div className="flex items-center border-b border-border pb-3">
                      <span className="text-sm text-muted-foreground w-40">Ciljna težina (kg)</span>
                      <span className="text-sm text-foreground">{activeGoal.targetValue}</span>
                    </div>
                  )}
                  {activeGoal.deadline && (
                    <div className="flex items-center">
                      <span className="text-sm text-muted-foreground w-40">Rok</span>
                      <span className="text-sm text-foreground">{new Date(activeGoal.deadline).toLocaleDateString('sr-RS')}</span>
                    </div>
                  )}
                </>
              ) : (
                <p className="text-sm text-muted-foreground">Nemate postavljen cilj. Kliknite "Postavi cilj" da definišete svoj fitness cilj.</p>
              )}
            </div>
          )}
        </div>

        {/* Logout Section */}
        <div className="bg-card border border-border rounded-lg p-6 flex items-center justify-between">
          <span className="text-sm text-muted-foreground">Želite li da se odjavite?</span>
          <button
            type="button"
            onClick={logout}
            className="bg-secondary border border-border text-foreground px-6 py-2 text-sm rounded hover:bg-secondary/80 transition-colors font-medium"
          >
            Odjavi se
          </button>
        </div>
      </div>
    </>
  );
}
