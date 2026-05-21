import { useState } from 'react';
import { useAuth } from './contexts/AuthContext';
import { api } from './services/api';

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

export function Profil() {
  const { user, logout } = useAuth();
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({
    username: user?.username || '',
    email: user?.email || '',
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
  });
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const f = (key) => (e) => setForm(prev => ({ ...prev, [key]: e.target.value }));

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      if (user?.id) {
        await api.updateUser(user.id, form);
        setSuccess('Profil uspješno ažuriran.');
      }
      setEditing(false);
    } catch {
      setError('Greška pri čuvanju profila.');
    } finally {
      setSaving(false);
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

      <div className="max-w-2xl space-y-6">
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
              <div className="grid grid-cols-2 gap-4">
                <Input label="Ime" value={form.firstName} onChange={f('firstName')} placeholder="Ime" />
                <Input label="Prezime" value={form.lastName} onChange={f('lastName')} placeholder="Prezime" />
              </div>
              <Input label="Korisničko ime" value={form.username} onChange={f('username')} placeholder="Username" />
              <Input label="Email" type="email" value={form.email} onChange={f('email')} placeholder="email@example.com" />
              <div className="flex gap-3 pt-2">
                <button
                  onClick={handleSave}
                  disabled={saving}
                  className="bg-primary text-white px-5 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                >
                  {saving ? 'Čuvanje...' : 'Sačuvaj'}
                </button>
                <button
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
                ['Ime', user?.firstName || '—'],
                ['Prezime', user?.lastName || '—'],
                ['Korisničko ime', user?.username || '—'],
                ['Email', user?.email || '—'],
                ['ID korisnika', user?.id || '—'],
              ].map(([label, value]) => (
                <div key={label} className="flex items-center border-b border-border pb-3 last:border-0 last:pb-0">
                  <span className="text-sm text-muted-foreground w-40">{label}</span>
                  <span className="text-sm text-foreground">{value}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Danger zone */}
        <div className="bg-card border border-destructive/30 rounded-lg p-6">
          <h3 className="text-base font-semibold text-foreground mb-1" style={BARLOW}>Zona opasnosti</h3>
          <p className="text-xs text-muted-foreground mb-4">Ove akcije su nepovratne.</p>
          <button
            onClick={logout}
            className="bg-destructive/10 border border-destructive/40 text-destructive px-4 py-2 text-sm rounded hover:bg-destructive/20 transition-colors"
          >
            Odjavi se
          </button>
        </div>
      </div>
    </>
  );
}
