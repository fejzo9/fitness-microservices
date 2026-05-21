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

function Field({ label, children }) {
  return (
    <div>
      <label className="block text-xs text-muted-foreground mb-1">{label}</label>
      {children}
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

export function Ishrana() {
  const [activeTab, setActiveTab] = useState('mealLogs');
  const [mealLogs, setMealLogs] = useState([]);
  const [mealItems, setMealItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // { type: 'log'|'item', data: null|obj }
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { fetchData(); }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [logs, items] = await Promise.all([api.getMealLogs(), api.getMealItems()]);
      setMealLogs(logs || []);
      setMealItems(items || []);
    } catch {
      setError('Greška pri učitavanju podataka');
    } finally {
      setLoading(false);
    }
  };

  const openModal = (type, data = null) => {
    setModal({ type, data });
    setForm(data || {});
    setError('');
  };
  const closeModal = () => { setModal(null); setForm({}); setError(''); };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    try {
      if (modal.type === 'log') {
        if (modal.data) await api.updateMealLog(modal.data.id, form);
        else await api.createMealLog(form);
      } else {
        if (modal.data) await api.updateMealItem(modal.data.id, form);
        else await api.createMealItem(form);
      }
      await fetchData();
      closeModal();
    } catch {
      setError('Greška pri čuvanju');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (type, id) => {
    if (!confirm('Obrisati stavku?')) return;
    try {
      if (type === 'log') await api.deleteMealLog(id);
      else await api.deleteMealItem(id);
      await fetchData();
    } catch {
      alert('Greška pri brisanju');
    }
  };

  const f = (key) => (e) => setForm(prev => ({ ...prev, [key]: e.target.value }));

  return (
    <>
      <div className="mb-6">
        <h2 className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3" style={BARLOW}>
          Ishrana
        </h2>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 bg-secondary border border-border rounded-lg p-1 w-fit">
        {[['mealLogs', 'Dnevnici obroka'], ['mealItems', 'Stavke obroka']].map(([key, label]) => (
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
          {/* Meal Logs Tab */}
          {activeTab === 'mealLogs' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{mealLogs.length} dnevnika</span>
                <button
                  onClick={() => openModal('log')}
                  className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors"
                >
                  + Novi dnevnik
                </button>
              </div>
              <div className="space-y-3">
                {mealLogs.length === 0 && (
                  <div className="bg-card border border-border rounded-lg p-8 text-center text-muted-foreground text-sm">
                    Nema dnevnika obroka. Dodajte prvi!
                  </div>
                )}
                {mealLogs.map((log) => (
                  <div key={log.id} className="bg-card border border-border rounded-lg p-4 flex items-center justify-between">
                    <div>
                      <div className="font-medium text-foreground text-sm" style={BARLOW}>{log.name || `Dnevnik #${log.id}`}</div>
                      <div className="text-xs text-muted-foreground mt-1">
                        {log.date && <span>{log.date}</span>}
                        {log.totalCalories != null && <span className="ml-3">🔥 {log.totalCalories} kcal</span>}
                        {log.notes && <span className="ml-3">{log.notes}</span>}
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <button
                        onClick={() => openModal('log', log)}
                        className="bg-secondary border border-border text-foreground px-3 py-1.5 text-xs rounded hover:bg-secondary/80 transition-colors"
                      >
                        Uredi
                      </button>
                      <button
                        onClick={() => handleDelete('log', log.id)}
                        className="bg-destructive/10 border border-destructive/30 text-destructive px-3 py-1.5 text-xs rounded hover:bg-destructive/20 transition-colors"
                      >
                        Obriši
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}

          {/* Meal Items Tab */}
          {activeTab === 'mealItems' && (
            <>
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-muted-foreground">{mealItems.length} stavki</span>
                <button
                  onClick={() => openModal('item')}
                  className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors"
                >
                  + Nova stavka
                </button>
              </div>
              <div className="bg-card border border-border rounded-lg overflow-hidden">
                {mealItems.length === 0 && (
                  <div className="p-8 text-center text-muted-foreground text-sm">
                    Nema stavki obroka. Dodajte prvu!
                  </div>
                )}
                {mealItems.length > 0 && (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border bg-secondary">
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Naziv</th>
                        <th className="text-left px-4 py-3 text-xs text-muted-foreground font-medium">Dnevnik ID</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Kalorije</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Proteini</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Ugljeni hidrati</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Masti</th>
                        <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Akcije</th>
                      </tr>
                    </thead>
                    <tbody>
                      {mealItems.map((item, i) => (
                        <tr key={item.id} className={`border-b border-border ${i % 2 === 0 ? 'bg-card' : 'bg-secondary/30'}`}>
                          <td className="px-4 py-3 text-foreground font-medium">{item.name || item.foodName || '—'}</td>
                          <td className="px-4 py-3 text-muted-foreground">#{item.mealLogId || '—'}</td>
                          <td className="px-4 py-3 text-right text-primary font-semibold">{item.calories ?? '—'}</td>
                          <td className="px-4 py-3 text-right text-muted-foreground">{item.protein != null ? `${item.protein}g` : '—'}</td>
                          <td className="px-4 py-3 text-right text-muted-foreground">{item.carbs != null ? `${item.carbs}g` : '—'}</td>
                          <td className="px-4 py-3 text-right text-muted-foreground">{item.fat != null ? `${item.fat}g` : '—'}</td>
                          <td className="px-4 py-3 text-right">
                            <div className="flex gap-2 justify-end">
                              <button
                                onClick={() => openModal('item', item)}
                                className="bg-secondary border border-border text-foreground px-2 py-1 text-xs rounded hover:bg-secondary/80"
                              >
                                Uredi
                              </button>
                              <button
                                onClick={() => handleDelete('item', item.id)}
                                className="bg-destructive/10 border border-destructive/30 text-destructive px-2 py-1 text-xs rounded hover:bg-destructive/20"
                              >
                                Obriši
                              </button>
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
        </>
      )}

      {/* Modal */}
      {modal && (
        <Modal
          title={modal.type === 'log' ? (modal.data ? 'Uredi dnevnik' : 'Novi dnevnik obroka') : (modal.data ? 'Uredi stavku' : 'Nova stavka obroka')}
          onClose={closeModal}
        >
          <div className="space-y-4">
            {error && <div className="bg-destructive/10 border border-destructive text-destructive px-3 py-2 rounded text-sm">{error}</div>}

            {modal.type === 'log' ? (
              <>
                <Field label="Naziv dnevnika">
                  <Input value={form.name || ''} onChange={f('name')} placeholder="npr. Ponedeljak ishrana" />
                </Field>
                <Field label="Datum">
                  <Input type="date" value={form.date || ''} onChange={f('date')} />
                </Field>
                <Field label="Ukupne kalorije">
                  <Input type="number" value={form.totalCalories || ''} onChange={f('totalCalories')} placeholder="npr. 2200" />
                </Field>
                <Field label="Napomena">
                  <Input value={form.notes || ''} onChange={f('notes')} placeholder="Napomena..." />
                </Field>
              </>
            ) : (
              <>
                <Field label="Naziv namirnice">
                  <Input value={form.name || form.foodName || ''} onChange={f('name')} placeholder="npr. Piletina 200g" />
                </Field>
                <Field label="ID dnevnika obroka">
                  <Input type="number" value={form.mealLogId || ''} onChange={f('mealLogId')} placeholder="ID dnevnika" />
                </Field>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Kalorije (kcal)">
                    <Input type="number" value={form.calories || ''} onChange={f('calories')} placeholder="0" />
                  </Field>
                  <Field label="Proteini (g)">
                    <Input type="number" value={form.protein || ''} onChange={f('protein')} placeholder="0" />
                  </Field>
                  <Field label="Ugljeni hidrati (g)">
                    <Input type="number" value={form.carbs || ''} onChange={f('carbs')} placeholder="0" />
                  </Field>
                  <Field label="Masti (g)">
                    <Input type="number" value={form.fat || ''} onChange={f('fat')} placeholder="0" />
                  </Field>
                </div>
                <Field label="Količina">
                  <Input value={form.quantity || ''} onChange={f('quantity')} placeholder="npr. 200g" />
                </Field>
              </>
            )}

            <div className="flex gap-3 pt-2">
              <button
                onClick={handleSave}
                disabled={saving}
                className="flex-1 bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
              >
                {saving ? 'Čuvanje...' : 'Sačuvaj'}
              </button>
              <button
                onClick={closeModal}
                className="flex-1 bg-secondary border border-border text-foreground px-4 py-2 text-sm rounded hover:bg-secondary/80 transition-colors"
              >
                Odustani
              </button>
            </div>
          </div>
        </Modal>
      )}
    </>
  );
}
