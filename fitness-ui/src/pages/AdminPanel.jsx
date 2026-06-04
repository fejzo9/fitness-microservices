import { useState, useEffect, useMemo } from 'react';
import { api } from '../services/api';

const BARLOW = { fontFamily: "'Barlow Condensed', sans-serif" };
const inputCls = "w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all";

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
      <div className="bg-card border border-border rounded-lg w-full max-w-lg shadow-xl animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between p-5 border-b border-border">
          <h3 className="text-base font-semibold text-foreground" style={BARLOW}>{title}</h3>
          <button type="button" onClick={onClose} className="text-muted-foreground hover:text-foreground text-xl leading-none cursor-pointer">&times;</button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  );
}

function Input({ label, ...props }) {
  return (
    <div>
      {label && <label className="block text-xs text-muted-foreground mb-1 font-medium">{label}</label>}
      <input className={inputCls} {...props} />
    </div>
  );
}

export function AdminPanel() {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [categories, setCategories] = useState([]);
  const [activeTab, setActiveTab] = useState('users');
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [userSearch, setUserSearch] = useState('');
  const [userRoleFilter, setUserRoleFilter] = useState('');
  const [userPage, setUserPage] = useState(0);
  const usersPerPage = 10;

  const [exData, setExData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [exPage, setExPage] = useState(0);
  const [exSearchInput, setExSearchInput] = useState('');
  const [exSearch, setExSearch] = useState('');
  const [exCatFilter, setExCatFilter] = useState(null);
  const [exDiffFilter, setExDiffFilter] = useState('');
  const exercisesPerPage = 10;

  useEffect(() => { fetchAll(); }, []);
  useEffect(() => { const t = setTimeout(() => setExSearch(exSearchInput), 300); return () => clearTimeout(t); }, [exSearchInput]);
  useEffect(() => { fetchExercises(); }, [exPage, exSearch, exCatFilter]);
  useEffect(() => { setUserPage(0); }, [userSearch, userRoleFilter]);
  useEffect(() => { setExPage(0); }, [exSearch, exCatFilter]);

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [u, r, cats] = await Promise.all([api.getUsers(), api.getRoles(), api.getExerciseCategories()]);
      setUsers(u || []); setRoles(r || []); setCategories(cats || []);
    } catch { setError('Gre\u0161ka pri u\u010ditavanju podataka'); }
    finally { setLoading(false); }
  };

  const fetchExercises = async () => {
    try {
      const data = await api.getExercises(exPage, exercisesPerPage, exSearch, exCatFilter);
      if (data && data.content) setExData(data);
      else if (Array.isArray(data)) setExData({ content: data, totalElements: data.length, totalPages: 1 });
      else setExData({ content: [], totalElements: 0, totalPages: 0 });
    } catch { setExData({ content: [], totalElements: 0, totalPages: 0 }); }
  };

  const openModal = (type, data = null) => { setModal({ type, data }); setForm(data || {}); setError(''); };
  const closeModal = () => { setModal(null); setForm({}); setError(''); };

  const handleSave = async () => {
    setSaving(true); setError('');
    try {
      if (modal.type === 'user') { if (modal.data) await api.updateUser(modal.data.id, form); else await api.createUser(form); }
      else if (modal.type === 'role') { if (modal.data) await api.updateRole(modal.data.id, form); else await api.createRole(form); }
      else if (modal.type === 'exercise') { if (modal.data) await api.updateExercise(modal.data.id, form); else await api.createExercise(form); }
      else if (modal.type === 'category') { if (modal.data) await api.updateExerciseCategory(modal.data.id, form); else await api.createExerciseCategory(form); }
      await fetchAll(); await fetchExercises(); closeModal();
    } catch { setError('Gre\u0161ka pri \u010duvanju'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (type, id) => {
    if (!confirm('Obrisati?')) return;
    try {
      if (type === 'user') await api.deleteUser(id);
      else if (type === 'role') await api.deleteRole(id);
      else if (type === 'exercise') await api.deleteExercise(id);
      else if (type === 'category') await api.deleteExerciseCategory(id);
      await fetchAll(); await fetchExercises();
    } catch { alert('Gre\u0161ka pri brisanju'); }
  };

  const f = (key) => (e) => setForm(prev => ({ ...prev, [key]: e.target.value }));

  const trainerCount = users.filter(u => (u.roleName || '').toUpperCase() === 'TRAINER').length;

  const filteredUsers = useMemo(() => {
    return users.filter(u => {
      const text = `${u.firstName || ''} ${u.lastName || ''} ${u.username || ''}`.toLowerCase();
      const matchSearch = !userSearch || text.includes(userSearch.toLowerCase()) || (u.email || '').toLowerCase().includes(userSearch.toLowerCase());
      const matchRole = !userRoleFilter || (u.roleName || '').toUpperCase() === userRoleFilter.toUpperCase();
      return matchSearch && matchRole;
    });
  }, [users, userSearch, userRoleFilter]);

  const userTotalPages = Math.ceil(filteredUsers.length / usersPerPage);
  const paginatedUsers = filteredUsers.slice(userPage * usersPerPage, (userPage + 1) * usersPerPage);

  const displayExercises = exDiffFilter ? exData.content.filter(ex => (ex.difficulty || '').toLowerCase() === exDiffFilter.toLowerCase()) : exData.content;

  const formatDate = (d) => { if (!d) return '\u2014'; try { return new Date(d).toLocaleDateString('sr-Latn', { day: '2-digit', month: '2-digit', year: 'numeric' }); } catch { return '\u2014'; } };
  const getRoleBadge = (r) => { const n = (r || '').toUpperCase(); if (n === 'ADMIN') return { cls: 'bg-primary/15 text-primary border-primary/30', label: 'Admin' }; if (n === 'TRAINER') return { cls: 'bg-blue-900/40 text-blue-400 border-blue-800/50', label: 'Trener' }; return { cls: 'bg-secondary text-foreground border-border', label: r || 'Korisnik' }; };
  const getDiffBadge = (d) => { const v = (d || '').toLowerCase(); if (v === 'beginner') return { cls: 'bg-emerald-900/40 text-emerald-400 border-emerald-800/50', label: 'Po\u010detnik' }; if (v === 'intermediate') return { cls: 'bg-amber-900/40 text-amber-400 border-amber-800/50', label: 'Srednji' }; if (v === 'advanced') return { cls: 'bg-red-900/40 text-red-400 border-red-800/50', label: 'Napredni' }; return { cls: 'bg-secondary text-muted-foreground border-border', label: d || '\u2014' }; };
  const pageNums = (cur, tot) => { if (tot <= 5) return Array.from({ length: tot }, (_, i) => i); if (cur < 3) return [0,1,2,3,4]; if (cur > tot - 3) return [tot-5,tot-4,tot-3,tot-2,tot-1]; return [cur-2,cur-1,cur,cur+1,cur+2]; };

  return (
    <>
      <div className="mb-6 flex items-center gap-3">
        <div className="w-10 h-10 bg-primary rounded flex items-center justify-center flex-shrink-0">
          <span className="text-white text-lg font-bold" style={BARLOW}>FT</span>
        </div>
        <h2 className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3 flex-1" style={BARLOW}>Admin Panel &middot; Upravljanje sistemom</h2>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        {[
          { label: 'Ukupno korisnika', value: users.length.toLocaleString(), sub: `+${users.filter(u => {
            const date = new Date(u.createdAt);
            const now = new Date();
            const diffTime = Math.abs(now - date);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            return diffDays <= 7;
          }).length} ove nedelje`, color: 'border-t-primary', tc: 'text-primary' },
          { label: 'Aktivni korisnici', value: users.length, sub: '100% od ukupno', color: 'border-t-emerald-500', tc: 'text-emerald-400' },
          { label: 'Vežbi u biblioteci', value: exData.totalElements, sub: `${categories.length} kategorija`, color: 'border-t-blue-500', tc: 'text-blue-400' },
          { label: 'Treneri', value: trainerCount, sub: 'sa nalozima', color: 'border-t-amber-500', tc: 'text-amber-400' },
        ].map(({ label, value, sub, color, tc }) => (
          <div key={label} className={`bg-card border border-border rounded-lg p-4 border-t-2 ${color}`}>
            <div className="text-xs text-muted-foreground mb-2 uppercase tracking-wide">{label}</div>
            <div className={`text-3xl font-bold ${tc} mb-1`} style={BARLOW}>{value}</div>
            <div className="text-xs text-muted-foreground">{sub}</div>
          </div>
        ))}
      </div>

      {/* Tab Navigation */}
      <div className="flex gap-1 mb-6 bg-secondary border border-border rounded-lg p-1 w-fit">
        {[{ id: 'users', label: 'Upravljanje korisnicima' }, { id: 'exercises', label: 'Biblioteka ve\u017ebi' }, { id: 'settings', label: 'Sistemske postavke' }].map(({ id, label }) => (
          <button key={id} onClick={() => setActiveTab(id)} className={`px-5 py-2 text-sm rounded-md transition-colors ${activeTab === id ? 'bg-primary text-white font-medium' : 'text-muted-foreground hover:text-foreground'}`}>{label}</button>
        ))}
      </div>

      {loading ? <div className="flex items-center justify-center h-40 text-muted-foreground">U\u010ditavanje...</div> : (
        <>
          {/* ═══ USERS TAB ═══ */}
          {activeTab === 'users' && (
            <div className="bg-card border border-border rounded-lg p-5">
              <div className="border-b border-border pb-3 mb-4 flex items-center justify-between">
                <h3 className="text-base font-bold text-foreground uppercase tracking-wide" style={BARLOW}>Lista korisnika</h3>
                <button type="button" onClick={() => openModal('user')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">+ Dodaj korisnika</button>
              </div>
              <div className="flex gap-3 mb-4">
                <input type="text" placeholder="Pretraži po imenu ili email-u..." value={userSearch} onChange={e => setUserSearch(e.target.value)} className={`flex-1 ${inputCls}`} />
                <select value={userRoleFilter} onChange={e => setUserRoleFilter(e.target.value)} className={inputCls.replace('w-full', 'w-auto')}>
                  <option value="">Sve uloge</option>
                  {roles.map(r => <option key={r.id} value={r.name}>{r.name}</option>)}
                </select>
                <select className={inputCls.replace('w-full', 'w-auto')}>
                  <option>Svi statusi</option>
                  <option>Aktivni</option>
                  <option>Neaktivni</option>
                </select>
              </div>
              <div className="border border-border rounded-lg overflow-hidden">
                <div className="grid grid-cols-12 border-b border-border bg-secondary">
                  {[{ l: 'Ime', s: 'col-span-3' },{ l: 'Email', s: 'col-span-3' },{ l: 'Uloga', s: 'col-span-2' },{ l: 'Status', s: 'col-span-2' },{ l: 'Akcije', s: 'col-span-2' }].map(({ l, s }, i) => (
                    <div key={l} className={`${s} p-3 ${i < 4 ? 'border-r border-border' : ''}`}><span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">{l}</span></div>
                  ))}
                </div>
                {paginatedUsers.length === 0 && <div className="p-8 text-center text-muted-foreground text-sm">Nema korisnika.</div>}
                {paginatedUsers.map(user => { const rb = getRoleBadge(user.roleName); return (
                  <div key={user.id} className="grid grid-cols-12 border-b border-border last:border-b-0 hover:bg-secondary/50 transition-colors">
                    <div className="col-span-3 p-3 border-r border-border">
                      <div className="text-sm text-foreground font-medium">{user.firstName || user.lastName ? `${user.firstName || ''} ${user.lastName || ''}`.trim() : user.username}</div>
                      <div className="text-xs text-muted-foreground">{formatDate(user.createdAt)}</div>
                    </div>
                    <div className="col-span-3 p-3 border-r border-border"><span className="text-sm text-muted-foreground">{user.email || '\u2014'}</span></div>
                    <div className="col-span-2 p-3 border-r border-border flex items-center justify-center"><span className={`text-xs px-2 py-1 rounded border font-medium ${rb.cls}`}>{rb.label}</span></div>
                    <div className="col-span-2 p-3 border-r border-border flex items-center justify-center">
                      <span className="text-xs px-2 py-1 rounded border bg-emerald-900/40 text-emerald-400 border-emerald-800/50">Aktivan</span>
                    </div>
                    <div className="col-span-2 p-3 flex gap-2 justify-center">
                      <button type="button" onClick={() => openModal('user', user)} className="bg-secondary border border-border text-foreground px-3 py-1 text-xs rounded hover:bg-secondary/80 transition-colors">Uredi</button>
                      <button type="button" onClick={() => handleDelete('user', user.id)} className="bg-destructive/15 border border-destructive/30 text-destructive px-3 py-1 text-xs rounded hover:bg-destructive/25 transition-colors">Obriši</button>
                    </div>
                  </div>
                ); })}
              </div>
              {filteredUsers.length > 0 && (
                <div className="flex items-center justify-between mt-4">
                  <span className="text-sm text-muted-foreground">Prikazano {userPage * usersPerPage + 1}&ndash;{Math.min((userPage + 1) * usersPerPage, filteredUsers.length)} od {filteredUsers.length}</span>
                  <div className="flex gap-2">
                    <button disabled={userPage === 0} onClick={() => setUserPage(p => p - 1)} className="bg-secondary border border-border text-muted-foreground px-3 py-1 text-sm rounded hover:bg-secondary/80 transition-colors disabled:opacity-40">◀ Prethodna</button>
                    {pageNums(userPage, userTotalPages).map(i => (
                      <button key={i} onClick={() => setUserPage(i)} className={`px-3 py-1 text-sm rounded font-medium transition-colors ${userPage === i ? 'bg-primary text-white' : 'bg-secondary border border-border text-foreground hover:bg-secondary/80'}`}>{i + 1}</button>
                    ))}
                    <button disabled={userPage >= userTotalPages - 1} onClick={() => setUserPage(p => p + 1)} className="bg-secondary border border-border text-foreground px-3 py-1 text-sm rounded hover:bg-secondary/80 transition-colors disabled:opacity-40">Sledeća ▶</button>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* ═══ EXERCISES TAB ═══ */}
          {activeTab === 'exercises' && (
            <div className="bg-card border border-border rounded-lg p-5">
              <div className="border-b border-border pb-3 mb-4 flex items-center justify-between">
                <h3 className="text-base font-bold text-foreground uppercase tracking-wide" style={BARLOW}>Upravljanje ve\u017ebama</h3>
                <button type="button" onClick={() => openModal('exercise')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">+ Dodaj ve\u017ebu</button>
              </div>
              <div className="flex gap-3 mb-4">
                <input type="text" placeholder="Pretra\u017ei ve\u017ebe..." value={exSearchInput} onChange={e => setExSearchInput(e.target.value)} className={`flex-1 ${inputCls}`} />
                <select value={exCatFilter || ''} onChange={e => setExCatFilter(e.target.value || null)} className={inputCls.replace('w-full', 'w-auto')}>
                  <option value="">Sve kategorije</option>
                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
                <select value={exDiffFilter} onChange={e => setExDiffFilter(e.target.value)} className={inputCls.replace('w-full', 'w-auto')}>
                  <option value="">Svi nivoi</option>
                  <option value="beginner">Po\u010detnik</option>
                  <option value="intermediate">Srednji</option>
                  <option value="advanced">Napredni</option>
                </select>
              </div>
              <div className="border border-border rounded-lg overflow-hidden">
                <div className="grid grid-cols-12 border-b border-border bg-secondary">
                  {['Naziv vežbe','Kategorija','Nivo','Opis','Akcije'].map((h, i) => {
                    const spans = ['col-span-4','col-span-2','col-span-2','col-span-2','col-span-2'];
                    return (
                      <div key={h} className={`${spans[i]} p-3 ${i < 4 ? 'border-r border-border' : ''}`}>
                        <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">{h}</span>
                      </div>
                    );
                  })}
                </div>
                {displayExercises.length === 0 && <div className="p-8 text-center text-muted-foreground text-sm">Nema vežbi.</div>}
                {displayExercises.map(ex => { const db = getDiffBadge(ex.difficulty); return (
                  <div key={ex.id} className="grid grid-cols-12 border-b border-border last:border-b-0 hover:bg-secondary/50 transition-colors">
                    <div className="col-span-4 p-3 border-r border-border"><span className="text-sm font-medium text-foreground">{ex.name}</span></div>
                    <div className="col-span-2 p-3 border-r border-border text-center">{ex.categories?.length > 0 ? ex.categories.map((c, j) => <span key={j} className="text-xs bg-primary/10 text-primary px-1.5 py-0.5 rounded mr-1 inline-block mb-1">{c.name}</span>) : <span className="text-xs text-muted-foreground">&mdash;</span>}</div>
                    <div className="col-span-2 p-3 border-r border-border flex items-center justify-center"><span className={`text-xs px-2 py-1 rounded border ${db.cls}`}>{db.label}</span></div>
                    <div className="col-span-2 p-3 border-r border-border text-center"><span className="text-sm text-muted-foreground truncate block">{ex.description || '—'}</span></div>
                    <div className="col-span-2 p-3 flex gap-2 justify-center">
                      <button type="button" onClick={() => openModal('exercise', ex)} className="bg-secondary border border-border text-foreground px-3 py-1 text-xs rounded hover:bg-secondary/80 transition-colors">Uredi</button>
                      <button type="button" onClick={() => handleDelete('exercise', ex.id)} className="bg-destructive/15 border border-destructive/30 text-destructive px-3 py-1 text-xs rounded hover:bg-destructive/25 transition-colors">Obriši</button>
                    </div>
                  </div>
                ); })}
              </div>
              {exData.totalElements > 0 && (
                <div className="flex items-center justify-between mt-4">
                  <span className="text-sm text-muted-foreground">Ukupno {exData.totalElements} ve\u017ebi</span>
                  <div className="flex gap-2">
                    <button disabled={exPage === 0} onClick={() => setExPage(p => p - 1)} className="bg-secondary border border-border text-muted-foreground px-3 py-1 text-sm rounded hover:bg-secondary/80 transition-colors disabled:opacity-40">◀ Prethodna</button>
                    {pageNums(exPage, exData.totalPages).map(i => (
                      <button key={i} onClick={() => setExPage(i)} className={`px-3 py-1 text-sm rounded font-medium transition-colors ${exPage === i ? 'bg-primary text-white' : 'bg-secondary border border-border text-foreground hover:bg-secondary/80'}`}>{i + 1}</button>
                    ))}
                    <button disabled={exPage >= exData.totalPages - 1} onClick={() => setExPage(p => p + 1)} className="bg-secondary border border-border text-muted-foreground px-3 py-1 text-sm rounded hover:bg-secondary/80 transition-colors disabled:opacity-40">Sledeća ▶</button>
                  </div>
                </div>
              )}
              <div className="mt-5 border-t border-border pt-4">
                <h4 className="text-sm font-bold text-foreground uppercase tracking-wide mb-3" style={BARLOW}>Upravljanje kategorijama</h4>
                <div className="flex gap-2 flex-wrap">
                  {categories.map(cat => (
                    <span key={cat.id} className="bg-secondary border border-border text-muted-foreground px-3 py-1 text-xs rounded inline-flex items-center gap-2">
                      {cat.name}
                      <button type="button" onClick={() => handleDelete('category', cat.id)} className="text-destructive hover:text-destructive/80 font-bold" title="Obri\u0161i">&times;</button>
                    </span>
                  ))}
                  <button type="button" onClick={() => openModal('category')} className="bg-primary/15 border border-primary/30 text-primary px-3 py-1 text-xs rounded hover:bg-primary/25 transition-colors">+ Dodaj kategoriju</button>
                </div>
              </div>
            </div>
          )}

          {/* System Settings Section */}
          {activeTab === 'settings' && (
            <div className="space-y-5">
              {/* General Settings */}
              <div className="bg-card border border-border rounded-lg p-5">
                <div className="border-b border-border pb-3 mb-4">
                  <h3
                    className="text-base font-bold text-foreground uppercase tracking-wide"
                    style={BARLOW}
                  >
                    Opšte postavke
                  </h3>
                </div>
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm text-muted-foreground mb-2">Naziv aplikacije</label>
                      <input type="text" defaultValue="Fitness i Trening Menadžer" className={`w-full ${inputCls}`} />
                    </div>
                    <div>
                      <label className="block text-sm text-muted-foreground mb-2">Jezik interfejsa</label>
                      <select className={`w-full ${inputCls}`}>
                        <option>Srpski</option>
                        <option>English</option>
                      </select>
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm text-muted-foreground mb-2">Email za kontakt</label>
                    <input type="email" defaultValue="admin@fitnessmanager.com" className={`w-full ${inputCls}`} />
                  </div>
                  <div className="flex items-center gap-3">
                    <input type="checkbox" id="enableRegistration" defaultChecked className="w-4 h-4 accent-primary" />
                    <label htmlFor="enableRegistration" className="text-sm text-foreground">Omogući registraciju novih korisnika</label>
                  </div>
                  <div className="flex items-center gap-3">
                    <input type="checkbox" id="requireEmailVerification" className="w-4 h-4 accent-primary" />
                    <label htmlFor="requireEmailVerification" className="text-sm text-foreground">Zahtevaj verifikaciju email adrese</label>
                  </div>
                </div>
              </div>

              {/* User Permissions */}
              <div className="bg-card border border-border rounded-lg p-5">
                <div className="border-b border-border pb-3 mb-4">
                  <h3
                    className="text-base font-bold text-foreground uppercase tracking-wide"
                    style={BARLOW}
                  >
                    Dozvole po ulogama
                  </h3>
                </div>
                <div className="border border-border rounded-lg overflow-hidden">
                  <div className="grid grid-cols-5 border-b border-border bg-secondary">
                    {['Dozvola','Admin','Trener','Korisnik','Akcije'].map((h, i) => (
                      <div key={h} className={`p-3 ${i < 4 ? 'border-r border-border' : ''}`}>
                        <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">{h}</span>
                      </div>
                    ))}
                  </div>
                  {[
                    { name: 'Upravljanje korisnicima', admin: true, trainer: false, user: false },
                    { name: 'Kreiranje planova treninga', admin: true, trainer: true, user: false },
                    { name: 'Pristup biblioteci vežbi', admin: true, trainer: true, user: true },
                    { name: 'Dodavanje vežbi', admin: true, trainer: true, user: false },
                    { name: 'Praćenje sopstvenog napretka', admin: true, trainer: true, user: true },
                  ].map((p, i, arr) => (
                    <div key={i} className={`grid grid-cols-5 hover:bg-secondary/50 transition-colors ${i < arr.length - 1 ? 'border-b border-border' : ''}`}>
                      <div className="p-3 border-r border-border">
                        <span className="text-sm text-foreground">{p.name}</span>
                      </div>
                      {[p.admin, p.trainer, p.user].map((val, j) => (
                        <div key={j} className="p-3 border-r border-border text-center">
                          <input type="checkbox" defaultChecked={val} className="w-4 h-4 accent-primary" />
                        </div>
                      ))}
                      <div className="p-3 text-center">
                        <button className="bg-secondary border border-border text-foreground px-3 py-1 text-xs rounded hover:bg-secondary/80 transition-colors">Sačuvaj</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Roles */}
              <div className="bg-card border border-border rounded-lg p-5">
                <div className="border-b border-border pb-3 mb-4 flex items-center justify-between">
                  <h3 className="text-base font-bold text-foreground uppercase tracking-wide" style={BARLOW}>Upravljanje ulogama</h3>
                  <button type="button" onClick={() => openModal('role')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">+ Nova uloga</button>
                </div>
                <div className="grid grid-cols-3 gap-4">
                  {roles.length === 0 && <div className="col-span-3 text-center text-muted-foreground text-sm py-4">Nema uloga.</div>}
                  {roles.map(role => (
                    <div key={role.id} className="bg-secondary border border-border rounded-lg p-4">
                      <div className="flex justify-between items-start mb-2">
                        <div className="font-semibold text-foreground" style={BARLOW}>{role.name}</div>
                        <span className="text-xs text-muted-foreground">#{role.id}</span>
                      </div>
                      {role.description && <p className="text-xs text-muted-foreground mb-3">{role.description}</p>}
                      <div className="flex gap-2">
                        <button type="button" onClick={() => openModal('role', role)} className="bg-card border border-border text-foreground px-3 py-1.5 text-xs rounded hover:bg-secondary transition-colors">Uredi</button>
                        <button type="button" onClick={() => handleDelete('role', role.id)} className="bg-destructive/15 border border-destructive/30 text-destructive px-3 py-1.5 text-xs rounded hover:bg-destructive/25 transition-colors">Obriši</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* System Maintenance */}
              <div className="bg-card border border-border rounded-lg p-5">
                <div className="border-b border-border pb-3 mb-4">
                  <h3
                    className="text-base font-bold text-foreground uppercase tracking-wide"
                    style={BARLOW}
                  >
                    Održavanje sistema
                  </h3>
                </div>
                <div className="grid grid-cols-3 gap-4">
                  {[
                    { title: 'Baza podataka', action: 'Napravi bekap', info: `${users.length} korisnika u bazi` },
                    { title: 'Keširanje', action: 'Očisti keš', info: `${categories.length} kategorija keširano` },
                    { title: 'Logovi sistema', action: 'Preuzmi logove', info: 'Poslednja aktivnost: Danas' },
                  ].map(({ title, action, info }) => (
                    <div key={title} className="bg-secondary border border-border rounded-lg p-4">
                      <div
                        className="text-sm font-bold text-foreground uppercase tracking-wide mb-3"
                        style={BARLOW}
                      >
                        {title}
                      </div>
                      <button className="w-full bg-card border border-border text-foreground px-3 py-2 text-sm mb-2 rounded hover:bg-secondary transition-colors">
                        {action}
                      </button>
                      <div className="text-xs text-muted-foreground">{info}</div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Save Settings Button */}
              <div className="flex justify-end">
                <button className="bg-primary text-white px-6 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  Sačuvaj sve postavke
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {/* Modal */}
      {modal && (
        <Modal title={modal.type === 'user' ? (modal.data ? 'Uredi korisnika' : 'Novi korisnik') : modal.type === 'role' ? (modal.data ? 'Uredi ulogu' : 'Nova uloga') : modal.type === 'exercise' ? (modal.data ? 'Uredi ve\u017ebu' : 'Nova ve\u017eba') : (modal.data ? 'Uredi kategoriju' : 'Nova kategorija')} onClose={closeModal}>
          <div className="space-y-4">
            {error && <div className="bg-destructive/10 border border-destructive text-destructive px-3 py-2 rounded text-sm">{error}</div>}
            {modal.type === 'user' && (<>
              <div className="grid grid-cols-2 gap-3"><Input label="Ime" value={form.firstName || ''} onChange={f('firstName')} placeholder="Ime" /><Input label="Prezime" value={form.lastName || ''} onChange={f('lastName')} placeholder="Prezime" /></div>
              <Input label="Korisni\u010dko ime" value={form.username || ''} onChange={f('username')} placeholder="Username" />
              <Input label="Email" type="email" value={form.email || ''} onChange={f('email')} placeholder="email@example.com" />
              {!modal.data && <Input label="Lozinka" type="password" value={form.password || ''} onChange={f('password')} placeholder="Lozinka" />}
              <div><label className="block text-xs text-muted-foreground mb-1 font-medium">Uloga</label><select value={form.roleId || ''} onChange={(e) => setForm(prev => ({ ...prev, roleId: Number(e.target.value) }))} className={inputCls}><option value="">Odaberite ulogu</option>{roles.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}</select></div>
            </>)}
            {modal.type === 'role' && (<><Input label="Naziv uloge" value={form.name || ''} onChange={f('name')} placeholder="npr. TRAINER" /><Input label="Opis" value={form.description || ''} onChange={f('description')} placeholder="Opis uloge..." /></>)}
            {modal.type === 'exercise' && (<>
              <Input label="Naziv ve\u017ebe" value={form.name || ''} onChange={f('name')} placeholder="npr. Bench Press" />
              <div><label className="block text-xs text-muted-foreground mb-1 font-medium">Opis</label><textarea className={`w-full resize-none ${inputCls}`} rows={3} value={form.description || ''} onChange={(e) => setForm(prev => ({ ...prev, description: e.target.value }))} placeholder="Opis ve\u017ebe..." /></div>
              <div><label className="block text-xs text-muted-foreground mb-1 font-medium">Nivo te\u017eine</label><select value={form.difficulty || ''} onChange={f('difficulty')} className={inputCls}><option value="">Odaberite nivo</option><option value="beginner">Po\u010detnik</option><option value="intermediate">Srednji</option><option value="advanced">Napredni</option></select></div>
            </>)}
            {modal.type === 'category' && (<><Input label="Naziv kategorije" value={form.name || ''} onChange={f('name')} placeholder="npr. Snaga" /><Input label="Opis" value={form.description || ''} onChange={f('description')} placeholder="Opis kategorije..." /></>)}
            <div className="flex gap-3 pt-2">
              <button type="button" onClick={handleSave} disabled={saving} className="flex-1 bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors disabled:opacity-50">{saving ? '\u010cuvanje...' : 'Sa\u010duvaj'}</button>
              <button type="button" onClick={closeModal} className="flex-1 bg-secondary border border-border text-foreground px-4 py-2 text-sm rounded hover:bg-secondary/80 transition-colors">Odustani</button>
            </div>
          </div>
        </Modal>
      )}
    </>
  );
}
