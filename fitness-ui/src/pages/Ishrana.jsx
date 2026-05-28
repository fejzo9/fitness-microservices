import { useState, useEffect } from 'react';
import { api } from '../services/api';

const BARLOW = { fontFamily: "'Barlow Condensed', sans-serif" };

function Modal({ title, onClose, children }) {
  return (
      <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
        <div className="bg-card border border-border rounded-lg w-full max-w-lg shadow-xl animate-in fade-in zoom-in-95 duration-150">
          <div className="flex items-center justify-between p-5 border-b border-border">
            <h3 className="text-base font-semibold text-foreground" style={BARLOW}>{title}</h3>
            <button onClick={onClose} className="text-muted-foreground hover:text-foreground text-xl leading-none cursor-pointer">×</button>
          </div>
          <div className="p-5">{children}</div>
        </div>
      </div>
  );
}

function Field({ label, children }) {
  return (
      <div>
        <label className="block text-xs text-muted-foreground mb-1 font-medium">{label}</label>
        {children}
      </div>
  );
}

function Input({ ...props }) {
  return (
      <input
          className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
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
    } catch (err) {
      setError('Greška pri učitavanju podataka');
    } finally {
      setLoading(false);
    }
  };

  const openModal = (type, data = null) => {
    setError('');

    if (data) {
      // Ako uređujemo stavku, osiguravamo da name i foodName budu sinhronizovani
      setForm({
        ...data,
        name: data.name || data.foodName || ''
      });
    } else {
      // Ako pravimo novo, postavljamo podrazumevane vrednosti
      setForm(type === 'log' ? {
        name: '',
        date: new Date().toISOString().split('T')[0], // Danasnji datum kao default
        totalCalories: '',
        notes: ''
      } : {
        name: '',
        mealLogId: mealLogs.length > 0 ? mealLogs[0].id : '', // Selektuj prvi dnevnik ako postoji
        calories: '',
        protein: '',
        carbs: '',
        fat: '',
        quantity: ''
      });
    }
    setModal({ type, data });
  };

  const closeModal = () => { setModal(null); setForm({}); setError(''); };

  const handleSave = async (e) => {
    if (e) e.preventDefault();
    setSaving(true);
    setError('');

    // Priprema podataka i parsiranje brojeva (da backend ne baci grešku)
    const payload = { ...form };

    if (modal.type === 'log') {
      if (payload.totalCalories) payload.totalCalories = Number(payload.totalCalories);
    } else {
      // Sinhronizacija imena za svaki slučaj ako backend traži striktno jedno polje
      payload.foodName = payload.name;
      if (payload.mealLogId) payload.mealLogId = Number(payload.mealLogId);
      if (payload.calories) payload.calories = Number(payload.calories);
      if (payload.protein) payload.protein = Number(payload.protein);
      if (payload.carbs) payload.carbs = Number(payload.carbs);
      if (payload.fat) payload.fat = Number(payload.fat);
    }

    try {
      if (modal.type === 'log') {
        if (modal.data?.id) {
          await api.updateMealLog(modal.data.id, payload);
        } else {
          await api.createMealLog(payload);
        }
      } else {
        if (modal.data?.id) {
          await api.updateMealItem(modal.data.id, payload);
        } else {
          await api.createMealItem(payload);
        }
      }
      await fetchData();
      closeModal();
    } catch (err) {
      setError('Greška pri čuvanju. Proverite unete podatke.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (type, id) => {
    if (!confirm('Da li ste sigurni da želite obrisati ovu stavku?')) return;
    try {
      if (type === 'log') await api.deleteMealLog(id);
      else await api.deleteMealItem(id);
      await fetchData();
    } catch (err) {
      alert('Greška pri brisanju sa servera');
    }
  };

  const handleChange = (key) => (e) => {
    setForm(prev => ({ ...prev, [key]: e.target.value }));
  };

  return (
      <div className="p-1">
        <div className="mb-6">
          <h2 className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3" style={BARLOW}>
            Ishrana
          </h2>
        </div>

        {/* Tabs */}
        <div className="flex gap-1 mb-6 bg-secondary border border-border rounded-lg p-1 w-fit">
          {[
            ['mealLogs', 'Dnevnici obroka'],
            ['mealItems', 'Stavke obroka']
          ].map(([key, label]) => (
              <button
                  key={key}
                  type="button"
                  onClick={() => setActiveTab(key)}
                  className={`px-4 py-2 text-sm rounded-md transition-colors cursor-pointer ${
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
                          type="button"
                          onClick={() => openModal('log')}
                          className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors cursor-pointer"
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
                          <div key={log.id} className="bg-card border border-border rounded-lg p-4 flex items-center justify-between hover:border-border/80 transition-all shadow-sm">
                            <div>
                              <div className="font-medium text-foreground text-sm" style={BARLOW}>
                                {log.name || `Dnevnik #${log.id}`}
                              </div>
                              <div className="text-xs text-muted-foreground mt-1 flex flex-wrap gap-x-4 gap-y-1">
                                {log.date && <span>📅 {log.date}</span>}
                                {log.totalCalories != null && <span className="text-primary font-semibold">🔥 {log.totalCalories} kcal</span>}
                                {log.notes && <span className="italic text-muted-foreground/80">📝 {log.notes}</span>}
                              </div>
                            </div>
                            <div className="flex gap-2">
                              <button
                                  type="button"
                                  onClick={() => openModal('log', log)}
                                  className="bg-secondary border border-border text-foreground px-3 py-1.5 text-xs rounded hover:bg-secondary/80 transition-colors cursor-pointer"
                              >
                                Uredi
                              </button>
                              <button
                                  type="button"
                                  onClick={() => handleDelete('log', log.id)}
                                  className="bg-destructive/10 border border-destructive/30 text-destructive px-3 py-1.5 text-xs rounded hover:bg-destructive/20 transition-colors cursor-pointer"
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
                          type="button"
                          onClick={() => openModal('item')}
                          className="bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors cursor-pointer"
                      >
                        + Nova stavka
                      </button>
                    </div>
                    <div className="bg-card border border-border rounded-lg overflow-hidden shadow-sm overflow-x-auto">
                      {mealItems.length === 0 && (
                          <div className="p-8 text-center text-muted-foreground text-sm">
                            Nema stavki obroka. Dodajte prvu!
                          </div>
                      )}
                      {mealItems.length > 0 && (
                          <table className="w-full text-sm min-w-[600px]">
                            <thead>
                            <tr className="border-b border-border bg-secondary/50 text-left">
                              <th className="px-4 py-3 text-xs text-muted-foreground font-medium">Naziv</th>
                              <th className="px-4 py-3 text-xs text-muted-foreground font-medium">Pripada dnevniku</th>
                              <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Kalorije</th>
                              <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Proteini</th>
                              <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Ugljeni hidrati</th>
                              <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Masti</th>
                              <th className="text-right px-4 py-3 text-xs text-muted-foreground font-medium">Akcije</th>
                            </tr>
                            </thead>
                            <tbody>
                            {mealItems.map((item, i) => {
                              // Pronalaženje imena dnevnika radi boljeg UX-a u tabeli
                              const pripadajuciLog = mealLogs.find(l => l.id === item.mealLogId);
                              return (
                                  <tr key={item.id} className={`border-b border-border hover:bg-secondary/20 transition-colors ${i % 2 === 0 ? 'bg-card' : 'bg-secondary/10'}`}>
                                    <td className="px-4 py-3 text-foreground font-medium">{item.name || item.foodName || '—'} <span className="text-xs text-muted-foreground block font-normal">{item.quantity}</span></td>
                                    <td className="px-4 py-3 text-muted-foreground text-xs">
                                      {pripadajuciLog ? pripadajuciLog.name : `#${item.mealLogId || '—'}`}
                                    </td>
                                    <td className="px-4 py-3 text-right text-primary font-semibold">{item.calories ?? 0} kcal</td>
                                    <td className="px-4 py-3 text-right text-muted-foreground">{item.protein ?? 0}g</td>
                                    <td className="px-4 py-3 text-right text-muted-foreground">{item.carbs ?? 0}g</td>
                                    <td className="px-4 py-3 text-right text-muted-foreground">{item.fat ?? 0}g</td>
                                    <td className="px-4 py-3 text-right">
                                      <div className="flex gap-2 justify-end">
                                        <button
                                            type="button"
                                            onClick={() => openModal('item', item)}
                                            className="bg-secondary border border-border text-foreground px-2 py-1 text-xs rounded hover:bg-secondary/80 cursor-pointer"
                                        >
                                          Uredi
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => handleDelete('item', item.id)}
                                            className="bg-destructive/10 border border-destructive/30 text-destructive px-2 py-1 text-xs rounded hover:bg-destructive/20 cursor-pointer"
                                        >
                                          Obriši
                                        </button>
                                      </div>
                                    </td>
                                  </tr>
                              );
                            })}
                            </tbody>
                          </table>
                      )}
                    </div>
                  </>
              )}
            </>
        )}

        {/* Modal sa ugrađenim HTML form tagom radi native submita */}
        {modal && (
            <Modal
                title={modal.type === 'log' ? (modal.data ? 'Uredi dnevnik' : 'Novi dnevnik obroka') : (modal.data ? 'Uredi stavku' : 'Nova stavka obroka')}
                onClose={closeModal}
            >
              <form onSubmit={handleSave} className="space-y-4">
                {error && <div className="bg-destructive/10 border border-destructive text-destructive px-3 py-2 rounded text-sm">{error}</div>}

                {modal.type === 'log' ? (
                    <>
                      <Field label="Naziv dnevnika">
                        <Input value={form.name || ''} onChange={handleChange('name')} placeholder="npr. Ponedeljak ishrana" required />
                      </Field>
                      <Field label="Datum">
                        <Input type="date" value={form.date || ''} onChange={handleChange('date')} required />
                      </Field>
                      <Field label="Ukupne kalorije">
                        <Input type="number" value={form.totalCalories || ''} onChange={handleChange('totalCalories')} placeholder="npr. 2200" />
                      </Field>
                      <Field label="Napomena">
                        <Input value={form.notes || ''} onChange={handleChange('notes')} placeholder="Napomena..." />
                      </Field>
                    </>
                ) : (
                    <>
                      <Field label="Naziv namirnice">
                        <Input value={form.name || ''} onChange={handleChange('name')} placeholder="npr. Piletina belo meso" required />
                      </Field>

                      <Field label="Poveži sa dnevnikom obroka">
                        <select
                            value={form.mealLogId || ''}
                            onChange={handleChange('mealLogId')}
                            className="w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                            required
                        >
                          <option value="" disabled>Izaberi dnevnik...</option>
                          {mealLogs.map(log => (
                              <option key={log.id} value={log.id}>
                                {log.name} ({log.date})
                              </option>
                          ))}
                        </select>
                      </Field>

                      <div className="grid grid-cols-2 gap-3">
                        <Field label="Kalorije (kcal)">
                          <Input type="number" value={form.calories || ''} onChange={handleChange('calories')} placeholder="0" min="0" />
                        </Field>
                        <Field label="Proteini (g)">
                          <Input type="number" value={form.protein || ''} onChange={handleChange('protein')} placeholder="0" min="0" />
                        </Field>
                        <Field label="Ugljeni hidrati (g)">
                          <Input type="number" value={form.carbs || ''} onChange={handleChange('carbs')} placeholder="0" min="0" />
                        </Field>
                        <Field label="Masti (g)">
                          <Input type="number" value={form.fat || ''} onChange={handleChange('fat')} placeholder="0" min="0" />
                        </Field>
                      </div>

                      <Field label="Količina / Porcija">
                        <Input value={form.quantity || ''} onChange={handleChange('quantity')} placeholder="npr. 200g ili 1 komad" />
                      </Field>
                    </>
                )}

                <div className="flex gap-3 pt-2">
                  <button
                      type="submit"
                      disabled={saving}
                      className="flex-1 bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors disabled:opacity-50 cursor-pointer"
                  >
                    {saving ? 'Čuvanje...' : 'Sačuvaj'}
                  </button>
                  <button
                      type="button"
                      onClick={closeModal}
                      className="flex-1 bg-secondary border border-border text-foreground px-4 py-2 text-sm rounded hover:bg-secondary/80 transition-colors cursor-pointer"
                  >
                    Odustani
                  </button>
                </div>
              </form>
            </Modal>
        )}
      </div>
  );
}