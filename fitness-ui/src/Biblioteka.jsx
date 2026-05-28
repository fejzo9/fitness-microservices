import { useState, useEffect } from 'react';
import { api } from './services/api';

export function Biblioteka() {
  const [exercises, setExercises] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [activeFilter, setActiveFilter] = useState('Svi');

  useEffect(() => {
    fetchCategories();
    fetchExercises();
  }, [currentPage, selectedCategory, searchTerm]);

  const fetchCategories = async () => {
    try {
      const data = await api.getExerciseCategories();
      setCategories(data || []);
    } catch (err) {
      console.error('Error fetching categories:', err);
    }
  };

  const fetchExercises = async () => {
    try {
      setLoading(true);
      const data = await api.getExercises(currentPage, 9, searchTerm, selectedCategory);
      setExercises(data.content || []);
      setTotalPages(data.totalPages || 1);
      setTotalElements(data.totalElements || 0);
      setError(null);
    } catch (err) {
      setError('Greška pri učitavanju vježbi');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const getCategoryTag = (categories) => {
    if (!categories || categories.length === 0) return 'Opšte';
    return categories.map(cat => cat.name).join(' · ');
  };

  const getCategoryColor = (categories) => {
    if (!categories || categories.length === 0) return 'text-primary';
    const categoryName = categories[0]?.name?.toLowerCase() || '';
    if (categoryName.includes('kardio')) return 'text-emerald-400';
    if (categoryName.includes('fleksibilnost') || categoryName.includes('yoga')) return 'text-blue-400';
    return 'text-primary';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-muted-foreground">Učitavanje vježbi...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-destructive">{error}</div>
      </div>
    );
  }

  return (
    <>
      {/* Page Title */}
      <div className="mb-6">
        <h2
          className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3"
          style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
        >
          Biblioteka vježbi
        </h2>
      </div>

      {/* Search and Filter Section */}
      <div className="bg-card border border-border rounded-lg p-5 mb-6">
        <div className="grid grid-cols-12 gap-4">
          {/* Search Bar */}
          <div className="col-span-7">
            <label className="block text-sm text-muted-foreground mb-2">
              Pretraga vježbi
            </label>
            <div className="bg-secondary border border-border rounded px-4 py-2 flex items-center gap-2">
              <span className="text-muted-foreground text-sm">🔍</span>
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    setCurrentPage(0);
                    fetchExercises();
                  }
                }}
                className="bg-transparent text-sm text-foreground flex-1 outline-none placeholder:text-muted-foreground"
                placeholder="Unesite naziv vježbe..."
              />
            </div>
          </div>

          {/* Category Filter */}
          <div className="col-span-3">
            <label className="block text-sm text-muted-foreground mb-2">
              Kategorija
            </label>
            <select
              value={selectedCategory || ''}
              onChange={(e) => setSelectedCategory(e.target.value || null)}
              className="w-full bg-secondary border border-border rounded px-4 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            >
              <option value="">Sve kategorije</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>

          {/* Search Button */}
          <div className="col-span-2 flex items-end">
            <button
              type="button"
              onClick={() => {
                setCurrentPage(0);
                fetchExercises();
              }}
              className="w-full bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors"
            >
              Pretraži
            </button>
          </div>
        </div>

        {/* Quick Filter Tags */}
        <div className="flex gap-2 mt-4 pt-4 border-t border-border items-center flex-wrap">
          <div className="text-xs text-muted-foreground">Brzi filteri:</div>
          <button
            type="button"
            onClick={() => {
              setActiveFilter('Svi');
              setSelectedCategory(null);
            }}
            className={`px-3 py-1 text-xs rounded font-medium transition-colors ${
              activeFilter === 'Svi'
                ? 'bg-primary text-white'
                : 'bg-secondary border border-border text-foreground hover:bg-secondary/80'
            }`}
          >
            Svi
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              type="button"
              onClick={() => {
                setActiveFilter(cat.name);
                setSelectedCategory(cat.id);
              }}
              className={`px-3 py-1 text-xs rounded font-medium transition-colors ${
                activeFilter === cat.name
                  ? 'bg-primary text-white'
                  : 'bg-secondary border border-border text-foreground hover:bg-secondary/80'
              }`}
            >
              {cat.name}
            </button>
          ))}
        </div>
      </div>

      {/* Results Count */}
      <div className="mb-4 text-sm text-muted-foreground">
        Pronađeno: <span className="text-foreground font-medium">{totalElements} vježbi</span>
      </div>

      {/* Exercise Grid */}
      <div className="grid grid-cols-3 gap-5 mb-6">
        {exercises.map((exercise) => (
          <div key={exercise.id} className="bg-card border border-border rounded-lg overflow-hidden hover:border-primary/50 transition-colors group">
            <div className="bg-secondary border-b border-border p-3">
              <h3
                className="text-base font-bold text-foreground group-hover:text-primary transition-colors"
                style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
              >
                {exercise.name}
              </h3>
            </div>
            <div className="p-4">
              <span className={`text-xs font-medium ${getCategoryColor(exercise.categories)} bg-primary/10 px-2 py-1 rounded mb-3 inline-block`}>
                {getCategoryTag(exercise.categories)}
              </span>
              <p className="text-xs text-muted-foreground mb-4 leading-relaxed">
                {exercise.description || 'Nema opisa'}
              </p>
              <button type="button" className="w-full bg-secondary border border-border text-foreground px-3 py-2 text-xs rounded hover:bg-primary hover:text-white hover:border-primary transition-colors">
                + Dodaj u plan
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="bg-card border border-border rounded-lg p-4">
        <div className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">
            Prikazano {currentPage * 9 + 1}–{Math.min((currentPage + 1) * 9, totalElements)} od {totalElements}
          </div>
          <div className="flex gap-2">
            <button
              type="button"
              className="bg-secondary border border-border text-muted-foreground px-3 py-1 text-sm rounded hover:bg-secondary/80 transition-colors disabled:opacity-50"
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
            >
              ← Prethodna
            </button>
            {[...Array(totalPages)].map((_, i) => (
              <button
                key={i}
                type="button"
                className={`px-3 py-1 text-sm rounded ${
                  i === currentPage
                    ? 'bg-primary text-white font-medium'
                    : 'bg-secondary border border-border text-foreground hover:bg-secondary/80'
                }`}
                onClick={() => setCurrentPage(i)}
              >
                {i + 1}
              </button>
            ))}
            <button
              type="button"
              className="bg-secondary border border-border text-foreground px-3 py-1 text-sm rounded hover:bg-secondary/80 transition-colors disabled:opacity-50"
              onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
              disabled={currentPage === totalPages - 1}
            >
              Sledeća →
            </button>
          </div>
          <div className="text-sm text-muted-foreground">Ukupno: {totalPages} stranica</div>
        </div>
      </div>
    </>
  );
}
