import { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Spinner } from '../components/Spinner';

export function Ishrana() {
  const [mealName, setMealName] = useState('');
  const [calories, setCalories] = useState('');
  const [protein, setProtein] = useState('');
  const [carbs, setCarbs] = useState('');
  const [fats, setFats] = useState('');
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [meals, setMeals] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Get current user ID from localStorage (assuming it's stored after login)
  const userId = parseInt(localStorage.getItem('userId') || '1');

  useEffect(() => {
    fetchMealsForDate(selectedDate);
  }, [selectedDate]);

  const fetchMealsForDate = async (date) => {
    setLoading(true);
    setError('');
    try {
      const dateStr = date.toISOString().split('T')[0];
      const data = await api.getMealEntriesByUserAndDate(userId, dateStr);
      setMeals(data || []);
    } catch (err) {
      setError('Greška pri učitavanju obroka');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddMeal = async () => {
    if (!mealName || !calories || !protein || !carbs || !fats) {
      setError('Sva polja su obavezna');
      return;
    }

    try {
      const newMeal = {
        userId,
        entryDate: selectedDate.toISOString().split('T')[0],
        mealTime: new Date().toTimeString().slice(0, 5) + ':00',
        mealName,
        calories: parseFloat(calories),
        proteinG: parseFloat(protein),
        carbsG: parseFloat(carbs),
        fatsG: parseFloat(fats),
      };

      await api.createMealEntry(newMeal);

      // Clear form
      setMealName('');
      setCalories('');
      setProtein('');
      setCarbs('');
      setFats('');
      setError('');

      // Refresh meals
      await fetchMealsForDate(selectedDate);
    } catch (err) {
      setError('Greška pri dodavanju obroka');
      console.error(err);
    }
  };

  const handleDeleteMeal = async (id) => {
    if (!confirm('Da li ste sigurni da želite obrisati ovaj obrok?')) return;

    try {
      await api.deleteMealEntry(id);
      await fetchMealsForDate(selectedDate);
    } catch (err) {
      setError('Greška pri brisanju obroka');
      console.error(err);
    }
  };

  const changeDate = (days) => {
    const newDate = new Date(selectedDate);
    newDate.setDate(newDate.getDate() + days);
    setSelectedDate(newDate);
  };

  const totalCalories = meals.reduce((sum, m) => sum + (parseFloat(m.calories) || 0), 0);
  const totalProtein = meals.reduce((sum, m) => sum + (parseFloat(m.proteinG) || 0), 0);
  const totalCarbs = meals.reduce((sum, m) => sum + (parseFloat(m.carbsG) || 0), 0);
  const totalFats = meals.reduce((sum, m) => sum + (parseFloat(m.fatsG) || 0), 0);

  const inputCls = "w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors";

  const formatDate = (date) => {
    const months = ['Januar', 'Februar', 'Mart', 'April', 'Maj', 'Jun', 'Jul', 'Avgust', 'Septembar', 'Oktobar', 'Novembar', 'Decembar'];
    const day = date.getDate();
    const month = months[date.getMonth()];
    const year = date.getFullYear();
    return `${day}. ${month} ${year}.`;
  };

  const formatTime = (timeStr) => {
    if (!timeStr) return '';
    return timeStr.slice(0, 5);
  };

  return (
    <>
      {/* Page Title */}
      <div className="mb-6">
        <h2
          className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3"
          style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
        >
          Plan ishrane
        </h2>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mb-6 bg-destructive/10 border border-destructive text-destructive px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* Date Selector */}
      <div className="mb-6">
        <div className="bg-card border border-border rounded-lg p-4 flex flex-wrap items-center gap-2 md:gap-4">
          <span className="text-sm text-muted-foreground">Datum:</span>
          <button
            onClick={() => changeDate(-1)}
            className="bg-secondary border border-border text-foreground px-3 py-1 text-sm rounded hover:bg-secondary/80 transition-colors"
          >
            ◀
          </button>
          <span className="text-sm font-medium text-foreground">{formatDate(selectedDate)}</span>
          <button
            onClick={() => changeDate(1)}
            className="bg-secondary border border-border text-foreground px-3 py-1 text-sm rounded hover:bg-secondary/80 transition-colors"
          >
            ▶
          </button>
        </div>
      </div>

      {/* Add Meal Form */}
      <div className="mb-6">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3
              className="text-lg font-bold text-foreground uppercase tracking-wide"
              style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
            >
              Dodaj obrok
            </h3>
          </div>

          <div className="space-y-4">
            <div>
              <label className="block text-sm text-muted-foreground mb-2">Naziv obroka</label>
              <input
                type="text"
                value={mealName}
                onChange={(e) => setMealName(e.target.value)}
                className={inputCls}
                placeholder="npr. Doručak - Jaja sa hljebom"
              />
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 md:gap-4">
              <div>
                <label className="block text-sm text-muted-foreground mb-2">Kalorije (kcal)</label>
                <input type="number" value={calories} onChange={(e) => setCalories(e.target.value)} className={inputCls} placeholder="0" />
              </div>
              <div>
                <label className="block text-sm text-muted-foreground mb-2">Proteini (g)</label>
                <input type="number" value={protein} onChange={(e) => setProtein(e.target.value)} className={inputCls} placeholder="0" />
              </div>
              <div>
                <label className="block text-sm text-muted-foreground mb-2">Ugljeni hidrati (g)</label>
                <input type="number" value={carbs} onChange={(e) => setCarbs(e.target.value)} className={inputCls} placeholder="0" />
              </div>
              <div>
                <label className="block text-sm text-muted-foreground mb-2">Masti (g)</label>
                <input type="number" value={fats} onChange={(e) => setFats(e.target.value)} className={inputCls} placeholder="0" />
              </div>
            </div>

            <div>
              <button
                onClick={handleAddMeal}
                className="w-full sm:w-auto bg-primary text-white px-6 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors min-h-[44px]"
              >
                + Dodaj obrok
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Today's Meals Overview */}
      <div className="mb-6">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3
              className="text-lg font-bold text-foreground uppercase tracking-wide"
              style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
            >
              Današnji obroci
            </h3>
          </div>

          {/* Daily Summary */}
          <div className="bg-secondary rounded-lg p-4 mb-4">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 md:gap-4">
              <div className="text-center">
                <div
                  className="text-2xl font-bold text-primary mb-1"
                  style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
                >
                  {totalCalories.toFixed(0)}
                </div>
                <div className="text-xs text-muted-foreground">Ukupno kcal</div>
              </div>
              <div className="text-center">
                <div
                  className="text-2xl font-bold text-emerald-400 mb-1"
                  style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
                >
                  {totalProtein.toFixed(0)}g
                </div>
                <div className="text-xs text-muted-foreground">Proteini</div>
              </div>
              <div className="text-center">
                <div
                  className="text-2xl font-bold text-blue-400 mb-1"
                  style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
                >
                  {totalCarbs.toFixed(0)}g
                </div>
                <div className="text-xs text-muted-foreground">Ugljeni hidrati</div>
              </div>
              <div className="text-center">
                <div
                  className="text-2xl font-bold text-amber-400 mb-1"
                  style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
                >
                  {totalFats.toFixed(0)}g
                </div>
                <div className="text-xs text-muted-foreground">Masti</div>
              </div>
            </div>
          </div>

          {/* Loading */}
          {loading && (
            <Spinner size="md" className="p-8" />
          )}

          {/* Meals Table — desktop */}
          {!loading && (
            <div className="hidden md:block border border-border rounded-lg overflow-hidden">
              {/* Table Header */}
              <div className="grid grid-cols-8 border-b border-border bg-secondary">
                <div className="col-span-2 p-3 border-r border-border">
                  <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Naziv</span>
                </div>
                <div className="p-3 border-r border-border text-center">
                  <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Vreme</span>
                </div>
                <div className="p-3 border-r border-border text-center">
                  <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Kalorije</span>
                </div>
                <div className="p-3 border-r border-border text-center">
                  <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Proteini</span>
                </div>
                <div className="p-3 border-r border-border text-center">
                  <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Ugljeni h.</span>
                </div>
                <div className="p-3 border-r border-border text-center">
                  <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Masti</span>
                </div>
                <div className="p-3 text-center">
                  <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Akcije</span>
                </div>
              </div>

              {/* Table Body */}
              {meals.length === 0 ? (
                <div className="p-8 text-center text-sm text-muted-foreground">
                  Nema unetih obroka za danas
                </div>
              ) : (
                meals.map((meal) => (
                  <div
                    key={meal.id}
                    className="grid grid-cols-8 border-b border-border last:border-b-0 hover:bg-secondary/50 transition-colors"
                  >
                    <div className="col-span-2 p-3 border-r border-border">
                      <span className="text-sm text-foreground">{meal.mealName}</span>
                    </div>
                    <div className="p-3 border-r border-border text-center">
                      <span className="text-sm text-muted-foreground">{formatTime(meal.mealTime)}</span>
                    </div>
                    <div className="p-3 border-r border-border text-center">
                      <span className="text-sm font-medium text-primary">{parseFloat(meal.calories || 0).toFixed(0)}</span>
                    </div>
                    <div className="p-3 border-r border-border text-center">
                      <span className="text-sm text-emerald-400">{parseFloat(meal.proteinG || 0).toFixed(0)}g</span>
                    </div>
                    <div className="p-3 border-r border-border text-center">
                      <span className="text-sm text-blue-400">{parseFloat(meal.carbsG || 0).toFixed(0)}g</span>
                    </div>
                    <div className="p-3 border-r border-border text-center">
                      <span className="text-sm text-amber-400">{parseFloat(meal.fatsG || 0).toFixed(0)}g</span>
                    </div>
                    <div className="p-3 text-center">
                      <button
                        onClick={() => handleDeleteMeal(meal.id)}
                        className="bg-destructive/10 border border-destructive/30 text-destructive px-2 py-1 text-xs rounded hover:bg-destructive/20 transition-colors"
                      >
                        Obriši
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}

          {/* Meals Cards — mobilni layout */}
          {!loading && (
            <div className="md:hidden space-y-3">
              {meals.length === 0 ? (
                <div className="p-8 text-center text-sm text-muted-foreground">
                  Nema unetih obroka za danas
                </div>
              ) : (
                meals.map((meal) => (
                  <div key={meal.id} className="bg-secondary rounded-lg p-4 border border-border">
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <span className="text-sm font-medium text-foreground block">{meal.mealName}</span>
                        <span className="text-xs text-muted-foreground">{formatTime(meal.mealTime)}</span>
                      </div>
                      <button
                        onClick={() => handleDeleteMeal(meal.id)}
                        className="bg-destructive/10 border border-destructive/30 text-destructive px-2 py-1 text-xs rounded hover:bg-destructive/20 transition-colors min-h-[36px]"
                      >
                        Obriši
                      </button>
                    </div>
                    <div className="grid grid-cols-2 gap-2 text-xs">
                      <span className="text-primary">🔥 {parseFloat(meal.calories || 0).toFixed(0)} kcal</span>
                      <span className="text-emerald-400">💪 {parseFloat(meal.proteinG || 0).toFixed(0)}g proteini</span>
                      <span className="text-blue-400">🍞 {parseFloat(meal.carbsG || 0).toFixed(0)}g ugljeni h.</span>
                      <span className="text-amber-400">🥑 {parseFloat(meal.fatsG || 0).toFixed(0)}g masti</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      </div>
    </>
  );
}
