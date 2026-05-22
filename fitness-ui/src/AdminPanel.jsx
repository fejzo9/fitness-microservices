import { useState, useEffect } from 'react';
import { api } from './services/api';

const BARLOW = { fontFamily: "'Barlow Condensed', sans-serif" };

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
      <div className="bg-card border border-border rounded-lg w-full max-w-lg">
        <div className="flex items-center justify-between p-5 border-b border-border">
          <h3 className="text-base font-semibold text-foreground" style={BARLOW}>{title}</h3>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground text-xl leading-none">×</button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  );
}

function Input({ label, ...props }) {
  return (
    <div>
      {label && <label className="block text-xs text-muted-foreground mb-1">{label}</label>}
      <input
        className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
        {...props}
      />
    </div>
  );
}

export function AdminPanel() {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [exercises, setExercises] = useState([]);
  const [categories, setCategories] = useState([]);
  const [catMaps, setCatMaps] = useState([]);
  const [activeTab, setActiveTab] = useState('users');
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { fetchAll(); }, []);

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [u, r, ex, cats] = await Promise.all([
        api.getUsers(),
        api.getRoles(),
        api.getExercises(0, 100),
        api.getExerciseCategories(),
      ]);
      setUsers(u || []);
      setRoles(r || []);
      setExercises((ex?.content || ex) || []);
      setCategories(cats || []);
      setCatMaps([]);
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
      if (modal.type === 'user') {
        if (modal.data) await api.updateUser(modal.data.id, form);
        else await api.createUser(form);
      } else if (modal.type === 'role') {
        if (modal.data) await api.updateRole(modal.data.id, form);
        else await api.createRole(form);
      } else if (modal.type === 'exercise') {
        if (modal.data) await api.updateExercise(modal.data.id, form);
        else await api.createExercise(form);
      } else if (modal.type === 'category') {
        if (modal.data) await api.updateExerciseCategory(modal.data.id, form);
        else await api.createExerciseCategory(form);
      } else if (modal.type === 'catmap') {
        await api.createExerciseCategoryMap(form);
      }
      await fetchAll();
      closeModal();
    } catch { setError('Greška pri čuvanju'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (type, id) => {
    if (!confirm('Obrisati?')) return;
    try {
      if (type === 'user') await api.deleteUser(id);
      else if (type === 'role') await api.deleteRole(id);
      else if (type === 'exercise') await api.deleteExercise(id);
      else if (type === 'category') await api.deleteExerciseCategory(id);
      else if (type === 'catmap') await api.deleteExerciseCategoryMap(id);
      await fetchAll();
    } catch { alert('Greška pri brisanju'); }
  };

  const f = (key) => (e) => setForm(prev => ({ ...prev, [key]: e.target.value }));

  const TABS = [
    ['users', 'Korisnici', users.length],
    ['roles', 'Uloge', roles.length],
    ['exercises', 'Vježbe', exercises.length],
    ['categories', 'Kategorije', categories.length],
    ['catmaps', 'Mapiranja', catMaps.length],
  ];

  return (
    <>
      <div className="mb-6">
        <h2 className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3" style={BARLOW}>
          Admin Panel
        </h2>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-5 gap-3 mb-6">
        {TABS.map(([, label, count]) => (
          <div key={label} className="bg-card border border-border rounded-lg p-3 text-center">
            <div className="text-xs text-muted-foreground mb-1">{label}</div>
            <div className="text-xl font-bold text-primary" style={BARLOW}>{count}</div>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 bg-secondary border border-border rounded-lg p-1 w-fit flex-wrap">
        {TABS.map(([key, label]) => (
          <button
            key={key}
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
          {/* Users */}
          {activeTab === 'users' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{users.length} korisnika</span>
                <button onClick={() => openModal('user')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  + Novi korisnik
                </button>
              </div>
              <div className="bg-card border border-border rounded-lg overflow-hidden">
                {users.length === 0 && (
                  <div className="p-8 text-center text-muted-foreground text-sm">Nema korisnika.</div>
                )}
                {users.length > 0 && (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border bg-secondary">
                        {['ID', 'Korisničko ime', 'Email', 'Ime', 'Prezime', 'Uloga', 'Akcije'].map(h => (
                          <th key={h} className={`text-left px-4 py-3 text-xs text-muted-foreground font-medium ${h === 'Akcije' ? 'text-right' : ''}`}>{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {users.map((u, i) => (
                        <tr key={u.id} className={`border-b border-border ${i % 2 === 0 ? 'bg-card' : 'bg-secondary/30'}`}>
                          <td className="px-4 py-3 text-muted-foreground">#{u.id}</td>
                          <td className="px-4 py-3 text-foreground font-medium">{u.username || '—'}</td>
                          <td className="px-4 py-3 text-muted-foreground">{u.email || '—'}</td>
                          <td className="px-4 py-3 text-muted-foreground">{u.firstName || '—'}</td>
                          <td className="px-4 py-3 text-muted-foreground">{u.lastName || '—'}</td>
                          <td className="px-4 py-3">
                            {u.roles?.length > 0 ? u.roles.map((r, j) => (
                              <span key={j} className="text-xs bg-primary/10 text-primary px-1.5 py-0.5 rounded mr-1">{r.name || r}</span>
                            )) : <span className="text-xs text-muted-foreground">—</span>}
                          </td>
                          <td className="px-4 py-3 text-right">
                            <div className="flex gap-2 justify-end">
                              <button onClick={() => openModal('user', u)} className="bg-secondary border border-border text-foreground px-2 py-1 text-xs rounded hover:bg-secondary/80">Uredi</button>
                              <button onClick={() => handleDelete('user', u.id)} className="bg-destructive/10 border border-destructive/30 text-destructive px-2 py-1 text-xs rounded hover:bg-destructive/20">Obriši</button>
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

          {/* Roles */}
          {activeTab === 'roles' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{roles.length} uloga</span>
                <button onClick={() => openModal('role')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  + Nova uloga
                </button>
              </div>
              <div className="grid grid-cols-3 gap-4">
                {roles.length === 0 && (
                  <div className="col-span-3 bg-card border border-border rounded-lg p-8 text-center text-muted-foreground text-sm">Nema uloga.</div>
                )}
                {roles.map((role) => (
                  <div key={role.id} className="bg-card border border-border rounded-lg p-4">
                    <div className="flex justify-between items-start mb-2">
                      <div className="font-semibold text-foreground" style={BARLOW}>{role.name}</div>
                      <span className="text-xs text-muted-foreground">#{role.id}</span>
                    </div>
                    {role.description && <p className="text-xs text-muted-foreground mb-3">{role.description}</p>}
                    <div className="flex gap-2">
                      <button onClick={() => openModal('role', role)} className="bg-secondary border border-border text-foreground px-3 py-1.5 text-xs rounded hover:bg-secondary/80">Uredi</button>
                      <button onClick={() => handleDelete('role', role.id)} className="bg-destructive/10 border border-destructive/30 text-destructive px-3 py-1.5 text-xs rounded hover:bg-destructive/20">Obriši</button>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}

          {/* Exercises (admin view with full CRUD) */}
          {activeTab === 'exercises' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{exercises.length} vježbi</span>
                <button onClick={() => openModal('exercise')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  + Nova vježba
                </button>
              </div>
              <div className="bg-card border border-border rounded-lg overflow-hidden">
                {exercises.length === 0 && (
                  <div className="p-8 text-center text-muted-foreground text-sm">Nema vježbi.</div>
                )}
                {exercises.length > 0 && (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border bg-secondary">
                        {['ID', 'Naziv', 'Opis', 'Kategorije', 'Akcije'].map(h => (
                          <th key={h} className={`text-left px-4 py-3 text-xs text-muted-foreground font-medium ${h === 'Akcije' ? 'text-right' : ''}`}>{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {exercises.map((ex, i) => (
                        <tr key={ex.id} className={`border-b border-border ${i % 2 === 0 ? 'bg-card' : 'bg-secondary/30'}`}>
                          <td className="px-4 py-3 text-muted-foreground">#{ex.id}</td>
                          <td className="px-4 py-3 text-foreground font-medium">{ex.name || '—'}</td>
                          <td className="px-4 py-3 text-muted-foreground text-xs max-w-xs truncate">{ex.description || '—'}</td>
                          <td className="px-4 py-3">
                            {ex.categories?.length > 0
                              ? ex.categories.map((c, j) => <span key={j} className="text-xs bg-primary/10 text-primary px-1.5 py-0.5 rounded mr-1">{c.name}</span>)
                              : <span className="text-xs text-muted-foreground">—</span>}
                          </td>
                          <td className="px-4 py-3 text-right">
                            <div className="flex gap-2 justify-end">
                              <button onClick={() => openModal('exercise', ex)} className="bg-secondary border border-border text-foreground px-2 py-1 text-xs rounded hover:bg-secondary/80">Uredi</button>
                              <button onClick={() => handleDelete('exercise', ex.id)} className="bg-destructive/10 border border-destructive/30 text-destructive px-2 py-1 text-xs rounded hover:bg-destructive/20">Obriši</button>
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

          {/* Categories */}
          {activeTab === 'categories' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{categories.length} kategorija</span>
                <button onClick={() => openModal('category')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  + Nova kategorija
                </button>
              </div>
              <div className="grid grid-cols-3 gap-4">
                {categories.length === 0 && (
                  <div className="col-span-3 bg-card border border-border rounded-lg p-8 text-center text-muted-foreground text-sm">Nema kategorija.</div>
                )}
                {categories.map((cat) => (
                  <div key={cat.id} className="bg-card border border-border rounded-lg p-4">
                    <div className="flex justify-between items-start mb-2">
                      <div className="font-semibold text-foreground" style={BARLOW}>{cat.name}</div>
                      <span className="text-xs text-muted-foreground">#{cat.id}</span>
                    </div>
                    {cat.description && <p className="text-xs text-muted-foreground mb-3">{cat.description}</p>}
                    <div className="flex gap-2">
                      <button onClick={() => openModal('category', cat)} className="bg-secondary border border-border text-foreground px-3 py-1.5 text-xs rounded hover:bg-secondary/80">Uredi</button>
                      <button onClick={() => handleDelete('category', cat.id)} className="bg-destructive/10 border border-destructive/30 text-destructive px-3 py-1.5 text-xs rounded hover:bg-destructive/20">Obriši</button>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}

          {/* Category Maps */}
          {activeTab === 'catmaps' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{catMaps.length} mapiranja</span>
                <button onClick={() => openModal('catmap')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  + Novo mapiranje
                </button>
              </div>
              <div className="bg-card border border-border rounded-lg overflow-hidden">
                {catMaps.length === 0 && (
                  <div className="p-8 text-center text-muted-foreground text-sm">Nema mapiranja vježbi i kategorija.</div>
                )}
                {catMaps.length > 0 && (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border bg-secondary">
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">ID</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Vježba ID</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Kategorija ID</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Akcije</th>
                      </tr>
                    </thead>
                    <tbody>
                      {catMaps.map((m, i) => (
                        <tr key={m.id} className={`border-b border-border ${i % 2 === 0 ? 'bg-card' : 'bg-secondary/30'}`}>
                          <td className="px-4 py-3 text-muted-foreground">#{m.id}</td>
                          <td className="px-4 py-3 text-foreground">#{m.exerciseId ?? '—'}</td>
                          <td className="px-4 py-3 text-foreground">#{m.categoryId ?? '—'}</td>
                          <td className="px-4 py-3 text-right">
                            <button onClick={() => handleDelete('catmap', m.id)} className="bg-destructive/10 border border-destructive/30 text-destructive px-2 py-1 text-xs rounded hover:bg-destructive/20">Obriši</button>
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
            modal.type === 'user' ? (modal.data ? 'Uredi korisnika' : 'Novi korisnik') :
            modal.type === 'role' ? (modal.data ? 'Uredi ulogu' : 'Nova uloga') :
            modal.type === 'exercise' ? (modal.data ? 'Uredi vježbu' : 'Nova vježba') :
            modal.type === 'category' ? (modal.data ? 'Uredi kategoriju' : 'Nova kategorija') :
            'Novo mapiranje'
          }
          onClose={closeModal}
        >
          <div className="space-y-4">
            {error && <div className="bg-destructive/10 border border-destructive text-destructive px-3 py-2 rounded text-sm">{error}</div>}

            {modal.type === 'user' && (
              <>
                <div className="grid grid-cols-2 gap-3">
                  <Input label="Ime" value={form.firstName || ''} onChange={f('firstName')} placeholder="Ime" />
                  <Input label="Prezime" value={form.lastName || ''} onChange={f('lastName')} placeholder="Prezime" />
                </div>
                <Input label="Korisničko ime" value={form.username || ''} onChange={f('username')} placeholder="Username" />
                <Input label="Email" type="email" value={form.email || ''} onChange={f('email')} placeholder="email@example.com" />
                {!modal.data && <Input label="Lozinka" type="password" value={form.password || ''} onChange={f('password')} placeholder="Lozinka" />}
                <div>
                  <label className="block text-xs text-muted-foreground mb-1">ID uloge</label>
                  <select
                    value={form.roleId || ''}
                    onChange={(e) => setForm(prev => ({ ...prev, roleId: Number(e.target.value) }))}
                    className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  >
                    <option value="">Odaberite ulogu</option>
                    {roles.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
                  </select>
                </div>
              </>
            )}

            {modal.type === 'role' && (
              <>
                <Input label="Naziv uloge" value={form.name || ''} onChange={f('name')} placeholder="npr. TRAINER" />
                <Input label="Opis" value={form.description || ''} onChange={f('description')} placeholder="Opis uloge..." />
              </>
            )}

            {modal.type === 'exercise' && (
              <>
                <Input label="Naziv vježbe" value={form.name || ''} onChange={f('name')} placeholder="npr. Bench Press" />
                <div>
                  <label className="block text-xs text-muted-foreground mb-1">Opis</label>
                  <textarea
                    className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary resize-none"
                    rows={3}
                    value={form.description || ''}
                    onChange={(e) => setForm(prev => ({ ...prev, description: e.target.value }))}
                    placeholder="Opis vježbe..."
                  />
                </div>
              </>
            )}

            {modal.type === 'category' && (
              <>
                <Input label="Naziv kategorije" value={form.name || ''} onChange={f('name')} placeholder="npr. Snaga" />
                <Input label="Opis" value={form.description || ''} onChange={f('description')} placeholder="Opis kategorije..." />
              </>
            )}

            {modal.type === 'catmap' && (
              <>
                <div>
                  <label className="block text-xs text-muted-foreground mb-1">Vježba</label>
                  <select
                    value={form.exerciseId || ''}
                    onChange={(e) => setForm(prev => ({ ...prev, exerciseId: Number(e.target.value) }))}
                    className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  >
                    <option value="">Odaberite vježbu</option>
                    {exercises.map(ex => <option key={ex.id} value={ex.id}>{ex.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs text-muted-foreground mb-1">Kategorija</label>
                  <select
                    value={form.categoryId || ''}
                    onChange={(e) => setForm(prev => ({ ...prev, categoryId: Number(e.target.value) }))}
                    className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  >
                    <option value="">Odaberite kategoriju</option>
                    {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
              </>
            )}

            <div className="flex gap-3 pt-2">
              <button onClick={handleSave} disabled={saving} className="flex-1 bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors disabled:opacity-50">
                {saving ? 'Čuvanje...' : 'Sačuvaj'}
              </button>
              <button onClick={closeModal} className="flex-1 bg-secondary border border-border text-foreground px-4 py-2 text-sm rounded hover:bg-secondary/80 transition-colors">
                Odustani
              </button>
            </div>
          </div>
        </Modal>
      )}
    </>
  );
}
