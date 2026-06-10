import { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../contexts/ToastContext';

export function Napredak() {
  const { user } = useAuth();
  const toast = useToast();
  const [metrics, setMetrics] = useState({
    currentWeight: null,
    weightChange: null,
    weeklyWorkouts: null,
    workoutChange: null,
    avgCalories: null,
    totalWorkoutTime: null,
  });

  const [weightData, setWeightData] = useState([]);
  const [workoutData, setWorkoutData] = useState([]);
  const [calorieData, setCalorieData] = useState([]);
  const [calorieGoal, setCalorieGoal] = useState(null);
  const [exerciseProgress, setExerciseProgress] = useState([]);
  const [loading, setLoading] = useState(true);

  const formatDate = (dateStr) => {
    const d = new Date(dateStr);
    return d.toLocaleDateString('sr-Latn', { day: '2-digit', month: '2-digit' });
  };

  useEffect(() => {
    fetchMetrics();
  }, [user?.id]);

  const fetchMetrics = async () => {
    if (!user?.id) return;

    setLoading(true);
    try {
      const weightHistory = await api.getWeightHistoryByUserId(user.id);

      setMetrics(prev => ({ ...prev, currentWeight: user.weight }));

      if (weightHistory && weightHistory.length > 0) {
        const sortedWeight = [...weightHistory].sort((a, b) => new Date(a.entryDate) - new Date(b.entryDate));
        const oldest = sortedWeight[0];
        const latest = sortedWeight[sortedWeight.length - 1];

        setMetrics(prev => ({
          ...prev,
          weightChange: (oldest.weight - latest.weight).toFixed(1),
        }));

        const weightByWeek = {};
        sortedWeight.forEach(entry => {
          const date = new Date(entry.entryDate);
          const diff = date.getDate() - date.getDay() + (date.getDay() === 0 ? -6 : 1);
          const firstDay = new Date(date.setDate(diff)).toISOString().split('T')[0];
          if (!weightByWeek[firstDay]) weightByWeek[firstDay] = { sum: 0, count: 0 };
          weightByWeek[firstDay].sum += entry.weight;
          weightByWeek[firstDay].count += 1;
        });

        const weightChartData = Object.keys(weightByWeek).sort().map(week => ({
          date: week,
          weight: (weightByWeek[week].sum / weightByWeek[week].count).toFixed(1)
        }));
        setWeightData(weightChartData);
      }

      const completedExercisesList = await api.getCompletedWorkoutsByUserId(user.id);

      const workoutsByDate = {};
      completedExercisesList.forEach(ex => {
        const date = ex.scheduledDate;
        if (!workoutsByDate[date]) workoutsByDate[date] = { count: 0, duration: 0, date };
        workoutsByDate[date].count += 1;
      });

      const userWorkouts = Object.values(workoutsByDate).map(w => ({ date: w.date, count: 1 }));

      const now = new Date();
      const oneWeekAgo = new Date();
      oneWeekAgo.setDate(now.getDate() - 7);

      const workoutsThisWeek = userWorkouts.filter(w => new Date(w.date) >= oneWeekAgo);

      const twoWeeksAgo = new Date();
      twoWeeksAgo.setDate(now.getDate() - 14);
      const workoutsPrevWeek = userWorkouts.filter(w => {
        const d = new Date(w.date);
        return d >= twoWeeksAgo && d < oneWeekAgo;
      });

      setMetrics(prev => ({
        ...prev,
        weeklyWorkouts: workoutsThisWeek.length,
        workoutChange: workoutsThisWeek.length - workoutsPrevWeek.length,
        totalWorkoutTime: 0
      }));

      const workoutCountByDate = {};
      for (let i = 13; i >= 0; i--) {
        const d = new Date();
        d.setDate(d.getDate() - i);
        workoutCountByDate[d.toISOString().split('T')[0]] = 0;
      }
      userWorkouts.forEach(w => {
        if (workoutCountByDate.hasOwnProperty(w.date)) workoutCountByDate[w.date] += 1;
      });
      const frequencyChartData = Object.keys(workoutCountByDate).sort().map(date => ({
        date,
        count: workoutCountByDate[date]
      }));
      setWorkoutData(frequencyChartData);

      const completedExercisesData = await api.getCompletedExercisesByUserId(user.id);

      const exerciseStats = {};
      completedExercisesData.forEach(ce => {
        if (!exerciseStats[ce.exerciseName]) exerciseStats[ce.exerciseName] = [];
        const workout = userWorkouts.find(w => w.id === ce.completedWorkoutId);
        if (workout) {
          exerciseStats[ce.exerciseName].push({
            date: workout.date,
            reps: ce.repsDone,
            sets: ce.setsDone,
            volume: ce.repsDone * ce.setsDone
          });
        }
      });

      const progressList = Object.keys(exerciseStats).map(name => {
        const entries = exerciseStats[name].sort((a, b) => new Date(a.date) - new Date(b.date));
        const first = entries[0];
        const last = entries[entries.length - 1];
        const improvement = first.volume > 0 ? ((last.volume - first.volume) / first.volume * 100).toFixed(0) : 0;
        return {
          name,
          improvement,
          lastValue: last.volume,
          unit: 'volumen (set×rep)',
          trend: entries.map(e => e.volume).slice(-5)
        };
      });
      setExerciseProgress(progressList);

      const allMealEntries = await api.getMealEntriesByUser(user.id);

      const caloriesByDate = {};
      const last14Days = [];
      for (let i = 13; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        const dateStr = date.toISOString().split('T')[0];
        last14Days.push(dateStr);
        caloriesByDate[dateStr] = 0;
      }

      allMealEntries?.forEach(meal => {
        const dateStr = meal.entryDate;
        if (caloriesByDate.hasOwnProperty(dateStr)) {
          caloriesByDate[dateStr] += parseFloat(meal.calories || 0);
        }
      });

      setCalorieData(last14Days.map(date => ({ date, calories: Math.round(caloriesByDate[date]) })));

      const weekCalories = last14Days.slice(-7).map(d => caloriesByDate[d]);
      const avgCals = weekCalories.reduce((sum, c) => sum + c, 0) / 7;

      try {
        const fitnessGoal = await api.getActiveFitnessGoal(user.id);
        if (fitnessGoal?.dailyCalorieGoal) setCalorieGoal(fitnessGoal.dailyCalorieGoal);
      } catch (err) { /* Nema aktivnog cilja */ }

      setMetrics(prev => ({ ...prev, avgCalories: Math.round(avgCals) }));

    } catch (error) {
      toast('Greška pri učitavanju podataka o napretku. Provjerite konekciju i pokušajte ponovo.', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Y-axis labels — apsolutno pozicioniranje, isti math kao gridlinije
  const YAxisLabels = ({ labels, chartHeight }) => (
      <div className="absolute left-0 pr-2" style={{ top: '24px', height: `${chartHeight}px`, width: '3rem' }}>
        {labels.map((n, i) => (
            <span
                key={i}
                className="absolute right-0 text-xs text-muted-foreground leading-none"
                style={{
                  top: `${(i / (labels.length - 1)) * 100}%`,
                  transform: 'translateY(-50%)'
                }}
            >
          {n}
        </span>
        ))}
      </div>
  );

  // Gridlinije — apsolutno pozicioniranje, isti math kao YAxisLabels
  const GridLines = ({ count }) => (
      <div className="absolute inset-0">
        {Array.from({ length: count }).map((_, i) => (
            <div
                key={i}
                className="absolute w-full border-b border-border/40"
                style={{ top: `${(i / (count - 1)) * 100}%` }}
            />
        ))}
      </div>
  );

  return (
      <>
        {/* Page Title */}
        <div className="mb-6">
          <h2
              className="text-3xl font-bold text-foreground uppercase tracking-wide border-b border-border pb-3"
              style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
          >
            Praćenje napretka
          </h2>
        </div>

        {/* Key Metrics Summary Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <div className="bg-card border border-border rounded-lg p-4 border-t-2 border-t-primary">
            <div className="text-xs text-muted-foreground mb-2 uppercase tracking-wide">Trenutna težina</div>
            <div className="text-3xl font-bold text-primary mb-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
              {metrics.currentWeight ? `${metrics.currentWeight} kg` : '—'}
            </div>
            <div className="text-xs text-emerald-400">
              {metrics.weightChange !== null ? `${metrics.weightChange >= 0 ? '+' : ''}${metrics.weightChange} kg od početka` : '—'}
            </div>
          </div>

          <div className="bg-card border border-border rounded-lg p-4 border-t-2 border-t-emerald-500">
            <div className="text-xs text-muted-foreground mb-2 uppercase tracking-wide">Treninzi ove nedjelje</div>
            <div className="text-3xl font-bold text-emerald-400 mb-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
              {metrics.weeklyWorkouts ?? '—'}
            </div>
            <div className="text-xs text-emerald-400">
              {metrics.workoutChange !== null ? `${metrics.workoutChange >= 0 ? '+' : ''}${metrics.workoutChange} od prošle nedelje` : '—'}
            </div>
          </div>

          <div className="bg-card border border-border rounded-lg p-4 border-t-2 border-t-blue-500">
            <div className="text-xs text-muted-foreground mb-2 uppercase tracking-wide">Prosečan unos kalorija</div>
            <div className="text-3xl font-bold text-blue-400 mb-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
              {metrics.avgCalories ? metrics.avgCalories.toLocaleString() : '—'}
            </div>
            <div className="text-xs text-muted-foreground">kcal/dan ove nedelje</div>
          </div>

          <div className="bg-card border border-border rounded-lg p-4 border-t-2 border-t-amber-500">
            <div className="text-xs text-muted-foreground mb-2 uppercase tracking-wide">Ukupno vrijeme treninga</div>
            <div className="text-3xl font-bold text-amber-400 mb-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
              {metrics.totalWorkoutTime ? `${metrics.totalWorkoutTime} min` : '—'}
            </div>
            <div className="text-xs text-muted-foreground">ove nedelje</div>
          </div>
        </div>

        {/* Main Charts Row */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6 mb-6">

          {/* Body Weight Chart */}
          <div className="bg-card border border-border rounded-lg p-5">
            <div className="border-b border-border pb-3 mb-4">
              <h3 className="text-base font-bold text-foreground uppercase tracking-wide" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                Tjelesna težina
              </h3>
            </div>
            <div className="bg-secondary rounded-lg p-4 h-64 relative">
              {weightData.length > 0 ? (() => {
                const weights = weightData.slice(-8).map(d => parseFloat(d.weight));
                const actualMax = Math.max(...weights);
                const actualMin = Math.min(...weights);

                const yAxisMin = Math.max(0, Math.floor(actualMin - 2));
                const yAxisMax = Math.ceil(actualMax + 2);
                const yRange = yAxisMax - yAxisMin || 5;

                const yLabels = [];
                const step = yRange / 4;
                for (let i = 0; i < 5; i++) {
                  const val = yAxisMax - (step * i);
                  yLabels.push(val % 1 === 0 ? val : val.toFixed(1));
                }

                const chartHeight = 200;
                const chartWidth = 400;
                const displayData = weightData.slice(-8);
                const pointSpacing = chartWidth / (displayData.length > 1 ? displayData.length - 1 : 1);

                const points = displayData.map((d, i) => ({
                  x: displayData.length > 1 ? i * pointSpacing : chartWidth / 2,
                  y: chartHeight - ((parseFloat(d.weight) - yAxisMin) / yRange) * chartHeight,
                  weight: d.weight
                }));

                const polylinePoints = points.map(p => `${p.x},${p.y}`).join(' ');
                const polygonPoints = displayData.length > 1
                    ? `0,${chartHeight} ${polylinePoints} ${chartWidth},${chartHeight}`
                    : `${chartWidth/2},${chartHeight} ${points[0].x},${points[0].y} ${chartWidth/2},${chartHeight}`;

                return (
                    <>
                      <YAxisLabels labels={yLabels} chartHeight={chartHeight} />
                      <div className="ml-12 h-[200px] mt-2 border-l border-border border-b relative">
                        <GridLines count={5} />
                        <svg className="absolute inset-0 w-full h-full" viewBox={`0 0 ${chartWidth} ${chartHeight}`} preserveAspectRatio="none" style={{ overflow: 'visible' }}>
                          <defs>
                            <linearGradient id="weightGrad" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="0%" stopColor="#F24E1E" stopOpacity="0.3" />
                              <stop offset="100%" stopColor="#F24E1E" stopOpacity="0" />
                            </linearGradient>
                          </defs>
                          <polygon points={polygonPoints} fill="url(#weightGrad)" />
                          <polyline points={polylinePoints} fill="none" stroke="#F24E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" vectorEffect="non-scaling-stroke" />
                          {points.map((p, i) => (
                              <circle key={i} cx={p.x} cy={p.y} r="4" fill="#0D1017" stroke="#F24E1E" strokeWidth="2" vectorEffect="non-scaling-stroke" className="cursor-pointer">
                                <title>{p.weight} kg</title>
                              </circle>
                          ))}
                        </svg>
                        <div className="absolute -bottom-6 left-0 right-0 flex justify-between text-[10px] text-muted-foreground">
                          {displayData.map((d, i) => <span key={i}>{formatDate(d.date)}</span>)}
                        </div>
                      </div>
                    </>
                );
              })() : (
                  <div className="flex items-center justify-center h-full text-muted-foreground text-sm">Nema podataka</div>
              )}
            </div>
          </div>

          {/* Workouts Per Week — Bar Chart */}
          <div className="bg-card border border-border rounded-lg p-5">
            <div className="border-b border-border pb-3 mb-4">
              <h3 className="text-base font-bold text-foreground uppercase tracking-wide" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                Broj treninga po nedelji
              </h3>
            </div>
            <div className="bg-secondary rounded-lg p-4 h-64 relative">
              {workoutData.length > 0 ? (() => {
                const counts = workoutData.map(d => d.count);
                const actualMax = Math.max(...counts);
                const yAxisMax = Math.max(7, Math.ceil(actualMax + 1));

                // Labele: od yAxisMax do 0 (svaki integer)
                const yLabels = Array.from({ length: yAxisMax + 1 }, (_, i) => yAxisMax - i);
                const chartHeight = 200;

                return (
                    <>
                      <YAxisLabels labels={yLabels} chartHeight={chartHeight} />
                      <div className="ml-12 h-[200px] mt-2 border-l border-border border-b relative">
                        <GridLines count={yLabels.length} />
                        {/* Barovi */}
                        <div className="absolute inset-0 flex items-end justify-around px-3 pb-0 gap-1.5">
                          {workoutData.map((d, i) => {
                            const heightPct = yAxisMax > 0 ? (d.count / yAxisMax) * 100 : 0;
                            const isMax = d.count === actualMax && actualMax > 0;
                            return (
                                <div
                                    key={i}
                                    className="w-full rounded-t transition-all"
                                    style={{
                                      height: `${heightPct}%`,
                                      minHeight: d.count > 0 ? '4px' : '0px',
                                      backgroundColor: isMax ? '#F24E1E' : 'rgba(242,78,30,0.45)'
                                    }}
                                    title={`${d.count} treninga`}
                                />
                            );
                          })}
                        </div>
                        <div className="absolute -bottom-6 left-0 right-0 flex justify-around text-[10px] text-muted-foreground">
                          {workoutData.map((d, i) => <span key={i}>{formatDate(d.date)}</span>)}
                        </div>
                      </div>
                    </>
                );
              })() : (
                  <div className="flex items-center justify-center h-full text-muted-foreground text-sm">Nema podataka</div>
              )}
            </div>
          </div>
        </div>

        {/* Calorie Intake Chart - Full Width */}
        <div className="mb-6">
          <div className="bg-card border border-border rounded-lg p-5">
            <div className="border-b border-border pb-3 mb-4">
              <h3 className="text-base font-bold text-foreground uppercase tracking-wide" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                Unos kalorija · Poslednjih 14 dana
              </h3>
            </div>
            <div className="bg-secondary rounded-lg p-4 h-48 relative">
              {calorieData.length > 0 ? (() => {
                const actualMax = Math.max(...calorieData.map(d => d.calories));

                let maxCalories;
                if (calorieGoal && actualMax > calorieGoal * 0.3) {
                  maxCalories = Math.max(actualMax, calorieGoal);
                } else {
                  maxCalories = actualMax > 0 ? actualMax * 1.2 : 100;
                }

                const yAxisMin = 0;
                let yAxisMax;
                if (maxCalories <= 100) yAxisMax = Math.ceil(maxCalories / 10) * 10;
                else if (maxCalories <= 500) yAxisMax = Math.ceil(maxCalories / 50) * 50;
                else yAxisMax = Math.ceil(maxCalories / 500) * 500;

                const yRange = yAxisMax - yAxisMin || 100;

                const yLabels = [];
                const step = yRange / 4;
                for (let i = 0; i < 5; i++) yLabels.push(Math.round(yAxisMax - (step * i)));

                const chartHeight = 160;
                const chartWidth = 780;
                const pointSpacing = chartWidth / (calorieData.length - 1);

                const points = calorieData.map((d, i) => ({
                  x: i * pointSpacing,
                  y: chartHeight - ((d.calories - yAxisMin) / yRange) * chartHeight,
                  calories: d.calories
                }));

                const polylinePoints = points.map(p => `${p.x},${p.y}`).join(' ');
                const polygonPoints = `0,${chartHeight} ${polylinePoints} ${chartWidth},${chartHeight}`;

                const goalY = calorieGoal
                    ? chartHeight - ((calorieGoal - yAxisMin) / yRange) * chartHeight
                    : null;

                return (
                    <>
                      <YAxisLabels labels={yLabels} chartHeight={chartHeight} />
                      <div className="ml-12 h-[160px] mt-2 border-l border-border border-b relative">
                        <GridLines count={5} />
                        {goalY !== null && goalY >= 0 && goalY <= chartHeight && (
                            <div className="absolute left-0 right-0 border-t-2 border-dashed border-primary/60" style={{ top: `${goalY}px` }}>
                              <span className="absolute -top-3 right-2 text-xs text-primary/80 bg-secondary px-1">Cilj: {calorieGoal}</span>
                            </div>
                        )}
                        <svg className="absolute inset-0 w-full h-full" viewBox={`0 0 ${chartWidth} ${chartHeight}`} preserveAspectRatio="none" style={{ overflow: 'visible' }}>
                          <defs>
                            <linearGradient id="calGrad" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="0%" stopColor="#F24E1E" stopOpacity="0.3" />
                              <stop offset="100%" stopColor="#F24E1E" stopOpacity="0" />
                            </linearGradient>
                          </defs>
                          <polygon points={polygonPoints} fill="url(#calGrad)" />
                          <polyline points={polylinePoints} fill="none" stroke="#F24E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" vectorEffect="non-scaling-stroke" />
                          {points.map((p, i) => (
                              <circle key={i} cx={p.x} cy={p.y} r="3" fill="#F24E1E" stroke="hsl(var(--secondary))" strokeWidth="1" className="cursor-pointer">
                                <title>{p.calories} kcal</title>
                              </circle>
                          ))}
                        </svg>
                        <div className="absolute -bottom-6 left-0 right-0 flex justify-between text-xs text-muted-foreground">
                          {calorieData.map((d, i) => {
                            const day = new Date(d.date).getDate().toString().padStart(2, '0');
                            return <span key={i}>{day}</span>;
                          })}
                        </div>
                      </div>
                    </>
                );
              })() : (
                  <div className="flex items-center justify-center h-full text-muted-foreground text-sm">Nema podataka</div>
              )}
            </div>
          </div>
        </div>

        {/* Exercise Progress Table */}
        <div>
          <div className="bg-card border border-border rounded-lg p-5">
            <div className="border-b border-border pb-3 mb-4">
              <h3 className="text-base font-bold text-foreground uppercase tracking-wide" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                Napredak po vježbama · Maksimalno opterećenje
              </h3>
            </div>

            <div className="border border-border rounded-lg overflow-hidden">
              <div className="grid grid-cols-4 border-b border-border bg-secondary">
                {['Vježba', 'Napredak', 'Zadnji volumen', 'Trend (poslednjih 5)'].map((h, i) => (
                    <div key={h} className={`p-3 ${i < 3 ? 'border-r border-border' : ''}`}>
                      <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">{h}</span>
                    </div>
                ))}
              </div>

              {exerciseProgress.length > 0 ? (
                  exerciseProgress.map((ex, idx) => (
                      <div
                          key={idx}
                          className={`grid grid-cols-4 ${idx !== exerciseProgress.length - 1 ? 'border-b border-border' : ''} hover:bg-secondary/30 transition-colors`}
                      >
                        <div className="p-3 border-r border-border">
                          <span className="text-sm font-bold text-foreground">{ex.name}</span>
                        </div>
                        <div className="p-3 border-r border-border flex items-center gap-2">
                    <span className={`text-sm font-bold ${ex.improvement >= 0 ? 'text-emerald-500' : 'text-red-500'}`}>
                      {ex.improvement >= 0 ? '↑' : '↓'} {Math.abs(ex.improvement)}%
                    </span>
                        </div>
                        <div className="p-3 border-r border-border">
                          <span className="text-sm text-foreground font-medium">{ex.lastValue}</span>
                          <span className="text-[10px] text-muted-foreground ml-1 block">{ex.unit}</span>
                        </div>
                        <div className="p-3 flex items-end gap-1">
                          {ex.trend.map((val, i) => {
                            const max = Math.max(...ex.trend);
                            const height = max > 0 ? (val / max) * 100 : 0;
                            return (
                                <div key={i} className="flex-1 bg-primary/40 rounded-t-[1px]" style={{ height: `${height}%`, minHeight: '4px' }} />
                            );
                          })}
                        </div>
                      </div>
                  ))
              ) : (
                  <div className="p-10 text-center text-muted-foreground text-sm">
                    Nema dovoljno podataka za prikaz napretka po vježbama
                  </div>
              )}
            </div>
          </div>
        </div>
      </>
  );
}