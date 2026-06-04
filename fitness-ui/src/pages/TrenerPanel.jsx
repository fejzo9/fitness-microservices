import { useState, useEffect } from 'react';
import { api } from '../services/api';
import { LoadingSpinner } from '../components/Spinner';
import { useToast } from '../contexts/ToastContext';

const BARLOW = { fontFamily: "'Barlow Condensed', sans-serif" };

const inputCls = "w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all";

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
      <div className="bg-card border border-border rounded-lg w-full max-w-lg shadow-xl animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between p-5 border-b border-border">
          <h3 className="text-base font-semibold text-foreground" style={BARLOW}>{title}</h3>
          <button type="button" onClick={onClose} className="text-muted-foreground hover:text-foreground text-xl leading-none cursor-pointer">×</button>
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

export function TrenerPanel() {
  const [trainerClients, setTrainerClients] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [workoutPlans, setWorkoutPlans] = useState([]);
  const [workoutDays, setWorkoutDays] = useState([]);
  const [activeTab, setActiveTab] = useState('clients');
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({});
  const toast = useToast();
  const [saving, setSaving] = useState(false);

  useEffect(() => { fetchAll(); }, []);

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [tc, notifs] = await Promise.all([
        api.getTrainerClients(),
        api.getNotifications(),
      ]);
      setTrainerClients(tc || []);
      setNotifications(notifs || []);
    } catch {
      toast('Greška pri učitavanju podataka. Provjerite konekciju i pokušajte ponovo.', 'error');
    } finally {
      setLoading(false);
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

  const TABS = [
    ['clients', 'Klijenti', trainerClients.length],
    ['notifications', 'Obavijesti', notifications.length],
  ];

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
      <div className="grid grid-cols-2 gap-4 mb-6">
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
        <LoadingSpinner message="Učitavanje podataka..." size="lg" />
      ) : (
        <>
          {/* Clients */}
          {activeTab === 'clients' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{trainerClients.length} klijenata</span>
                <button type="button" onClick={() => openModal('client')} className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors">
                  + Dodaj klijenta
                </button>
              </div>
              <div className="bg-card border border-border rounded-lg overflow-hidden">
                {trainerClients.length === 0 && (
                  <div className="p-8 text-center text-muted-foreground text-sm">Nema klijenata. Dodajte prvog!</div>
                )}
                {trainerClients.length > 0 && (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border bg-secondary">
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">ID</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Trener ID</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Klijent ID</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Datum od</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Status</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Akcije</th>
                      </tr>
                    </thead>
                    <tbody>
                      {trainerClients.map((tc, i) => (
                        <tr key={tc.id} className={`border-b border-border ${i % 2 === 0 ? 'bg-card' : 'bg-secondary/30'}`}>
                          <td className="px-4 py-3 text-muted-foreground">#{tc.id}</td>
                          <td className="px-4 py-3 text-foreground">{tc.trainerId ?? '—'}</td>
                          <td className="px-4 py-3 text-foreground">{tc.clientId ?? '—'}</td>
                          <td className="px-4 py-3 text-muted-foreground">{tc.startDate || '—'}</td>
                          <td className="px-4 py-3">
                            <span className={`text-xs px-2 py-0.5 rounded ${tc.active !== false ? 'bg-emerald-500/20 text-emerald-400' : 'bg-muted text-muted-foreground'}`}>
                              {tc.active !== false ? 'Aktivan' : 'Neaktivan'}
                            </span>
                          </td>
                          <td className="px-4 py-3 text-right">
                            <div className="flex gap-2 justify-end">
                              <button type="button" onClick={() => openModal('client', tc)} className="bg-secondary border border-border text-foreground px-2 py-1 text-xs rounded hover:bg-secondary/80">Uredi</button>
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
