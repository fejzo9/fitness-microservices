import { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

export function Napredak() {
  const { user } = useAuth();
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

  useEffect(() => {
    fetchMetrics();
  }, [user?.id]);

  const fetchMetrics = async () => {
    if (!user?.id) return;

    setLoading(true);
    try {
      // Get current weight from user model
      setMetrics(prev => ({
        ...prev,
        currentWeight: user.weight,
      }));

      // Fetch progress entries for weight change calculation
      const progressEntries = await api.getProgressEntries();
      const userEntries = progressEntries?.filter(e => e.userId === user.id) || [];

      if (userEntries.length > 0) {
        const sortedEntries = [...userEntries].sort((a, b) => new Date(b.entryDate) - new Date(a.entryDate));
        const oldest = sortedEntries[sortedEntries.length - 1];
        if (oldest.weightKg && user.weight) {
          setMetrics(prev => ({
            ...prev,
            weightChange: (user.weight - oldest.weightKg).toFixed(1),
          }));
        }
      }

      // Fetch completed workouts
      const completedWorkouts = await api.getCompletedWorkouts();
      const userWorkouts = completedWorkouts?.filter(w => w.userId === user.id) || [];

      // Calculate weekly workouts (last 7 days)
      const oneWeekAgo = new Date();
      oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);

      const weeklyWorkouts = userWorkouts.filter(w => {
        const workoutDate = new Date(w.date);
        return workoutDate >= oneWeekAgo;
      });

      // Calculate total workout time for this week
      const totalMinutes = weeklyWorkouts.reduce((sum, w) => sum + (w.durationMinutes || 0), 0);

      // Fetch meal entries for last 14 days
      const fourteenDaysAgo = new Date();
      fourteenDaysAgo.setDate(fourteenDaysAgo.getDate() - 14);

      const allMealEntries = await api.getMealEntriesByUser(user.id);

      // Group meals by date and calculate daily calories
      const caloriesByDate = {};
      const last14Days = [];

      // Create array of last 14 dates
      for (let i = 13; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        const dateStr = date.toISOString().split('T')[0];
        last14Days.push(dateStr);
        caloriesByDate[dateStr] = 0;
      }


      // Sum calories by date
      allMealEntries?.forEach(meal => {
        const dateStr = meal.entryDate;
        if (caloriesByDate.hasOwnProperty(dateStr)) {
          caloriesByDate[dateStr] += parseFloat(meal.calories || 0);
        }
      });


      // Convert to array for chart
      const calorieChartData = last14Days.map(date => ({
        date,
        calories: Math.round(caloriesByDate[date])
      }));

      setCalorieData(calorieChartData);

      // Calculate average calories for the week
      const weekCalories = last14Days.slice(-7).map(d => caloriesByDate[d]);
      const avgCals = weekCalories.reduce((sum, c) => sum + c, 0) / 7;

      // Get fitness goal for calorie target
      try {
        const fitnessGoal = await api.getActiveFitnessGoal(user.id);
        if (fitnessGoal?.dailyCalorieGoal) {
          setCalorieGoal(fitnessGoal.dailyCalorieGoal);
        }
      } catch (err) {
        console.log('No active fitness goal');
      }

      setMetrics(prev => ({
        ...prev,
        weeklyWorkouts: weeklyWorkouts.length,
        totalWorkoutTime: totalMinutes,
        avgCalories: Math.round(avgCals),
      }));

    } catch (error) {
      console.error('Error fetching metrics:', error);
    } finally {
      setLoading(false);
    }
  };

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
      <div className="grid grid-cols-4 gap-4 mb-6">
        <div className="bg-card border border-border rounded-lg p-4 border-t-2 border-t-primary">
          <div className="text-xs text-muted-foreground mb-2 uppercase tracking-wide">Trenutna težina</div>
          <div
            className="text-3xl font-bold text-primary mb-1"
            style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
          >
            {metrics.currentWeight ? `${metrics.currentWeight} kg` : '—'}
          </div>
          <div className="text-xs text-emerald-400">
            {metrics.weightChange !== null ? `${metrics.weightChange >= 0 ? '+' : ''}${metrics.weightChange} kg od početka` : '—'}
          </div>
        </div>

        <div className="bg-card border border-border rounded-lg p-4 border-t-2 border-t-emerald-500">
          <div className="text-xs text-muted-foreground mb-2 uppercase tracking-wide">Treninzi ove nedjelje</div>
          <div
            className="text-3xl font-bold text-emerald-400 mb-1"
            style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
          >
            {metrics.weeklyWorkouts ?? '—'}
          </div>
          <div className="text-xs text-emerald-400">
            {metrics.workoutChange !== null ? `${metrics.workoutChange >= 0 ? '+' : ''}${metrics.workoutChange} od prošle nedelje` : '—'}
          </div>
        </div>

        <div className="bg-card border border-border rounded-lg p-4 border-t-2 border-t-blue-500">
          <div className="text-xs text-muted-foreground mb-2 uppercase tracking-wide">Prosečan unos kalorija</div>
          <div
            className="text-3xl font-bold text-blue-400 mb-1"
            style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
          >
            {metrics.avgCalories ? metrics.avgCalories.toLocaleString() : '—'}
          </div>
          <div className="text-xs text-muted-foreground">kcal/dan ove nedelje</div>
        </div>

        <div className="bg-card border border-border rounded-lg p-4 border-t-2 border-t-amber-500">
          <div className="text-xs text-muted-foreground mb-2 uppercase tracking-wide">Ukupno vrijeme treninga</div>
          <div
            className="text-3xl font-bold text-amber-400 mb-1"
            style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
          >
            {metrics.totalWorkoutTime ? `${metrics.totalWorkoutTime} min` : '—'}
          </div>
          <div className="text-xs text-muted-foreground">ove nedelje</div>
        </div>
      </div>

      {/* Main Charts Row */}
      <div className="grid grid-cols-2 gap-6 mb-6">
        {/* Body Weight Chart */}
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3
              className="text-base font-bold text-foreground uppercase tracking-wide"
              style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
            >
              Telesna težina · Poslednjih 8 nedelja
            </h3>
          </div>
          <div className="bg-secondary rounded-lg p-4 h-64 relative">
            {weightData.length > 0 ? (
              <>
                {/* Y-axis labels */}
                <div className="absolute left-0 top-0 bottom-0 flex flex-col justify-between text-xs text-muted-foreground pr-2 py-2">
                  <span>85kg</span>
                  <span>82kg</span>
                  <span>79kg</span>
                  <span>76kg</span>
                  <span>73kg</span>
                  <span>70kg</span>
                </div>
                {/* Chart */}
                <div className="ml-10 h-full border-l border-border border-b relative">
                  <div className="absolute inset-0 flex flex-col justify-between">
                    {[...Array(5)].map((_, i) => (
                      <div key={i} className="border-b border-border/40"></div>
                    ))}
                  </div>
                  <svg className="absolute inset-0 w-full h-full" style={{ overflow: 'visible' }}>
                    <defs>
                      <linearGradient id="weightGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#F24E1E" stopOpacity="0.3" />
                        <stop offset="100%" stopColor="#F24E1E" stopOpacity="0" />
                      </linearGradient>
                    </defs>
                    <polygon
                      points="10,40 70,35 130,45 190,50 250,42 310,38 370,30 430,25 430,200 10,200"
                      fill="url(#weightGrad)"
                    />
                    <polyline
                      points="10,40 70,35 130,45 190,50 250,42 310,38 370,30 430,25"
                      fill="none"
                      stroke="#F24E1E"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                    {[10, 70, 130, 190, 250, 310, 370, 430].map((x, i) => {
                      const ys = [40, 35, 45, 50, 42, 38, 30, 25];
                      return <circle key={i} cx={x} cy={ys[i]} r="4" fill="#F24E1E" stroke="#0D1017" strokeWidth="2" />;
                    })}
                  </svg>
                  <div className="absolute -bottom-6 left-0 right-0 flex justify-between text-xs text-muted-foreground">
                    {['N1','N2','N3','N4','N5','N6','N7','N8'].map(n => <span key={n}>{n}</span>)}
                  </div>
                </div>
              </>
            ) : (
              <div className="flex items-center justify-center h-full text-muted-foreground text-sm">Nema podataka</div>
            )}
          </div>
        </div>

        {/* Workouts Per Week Chart */}
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3
              className="text-base font-bold text-foreground uppercase tracking-wide"
              style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
            >
              Broj treninga po nedelji
            </h3>
          </div>
          <div className="bg-secondary rounded-lg p-4 h-64 relative">
            {workoutData.length > 0 ? (
              <>
                <div className="absolute left-0 top-0 bottom-0 flex flex-col justify-between text-xs text-muted-foreground pr-2 py-2">
                  {['7','6','5','4','3','2','1','0'].map(n => <span key={n}>{n}</span>)}
                </div>
                <div className="ml-10 h-full border-l border-border border-b relative">
                  <div className="absolute inset-0 flex flex-col justify-between">
                    {[...Array(7)].map((_, i) => (
                      <div key={i} className="border-b border-border/40"></div>
                    ))}
                  </div>
                  <div className="absolute inset-0 flex items-end justify-around px-3 pb-0 gap-1.5">
                    {[57, 71, 43, 57, 86, 71, 71, 71].map((h, i) => (
                      <div
                        key={i}
                        className="w-full rounded-t transition-all"
                        style={{ height: `${h}%`, backgroundColor: i === 4 ? '#F24E1E' : 'rgba(242,78,30,0.45)' }}
                      ></div>
                    ))}
                  </div>
                  <div className="absolute -bottom-6 left-0 right-0 flex justify-around text-xs text-muted-foreground">
                    {['N1','N2','N3','N4','N5','N6','N7','N8'].map(n => <span key={n}>{n}</span>)}
                  </div>
                </div>
              </>
            ) : (
              <div className="flex items-center justify-center h-full text-muted-foreground text-sm">Nema podataka</div>
            )}
          </div>
        </div>
      </div>

      {/* Calorie Intake Chart - Full Width */}
      <div className="mb-6">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3
              className="text-base font-bold text-foreground uppercase tracking-wide"
              style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
            >
              Unos kalorija · Poslednjih 14 dana
            </h3>
          </div>
          <div className="bg-secondary rounded-lg p-4 h-48 relative">
            {calorieData.length > 0 ? (
              (() => {

                // Calculate chart dimensions
                const actualMax = Math.max(...calorieData.map(d => d.calories));
                const actualMin = Math.min(...calorieData.map(d => d.calories));

                // Set reasonable Y-axis range - only include goal if it's within reasonable range of actual data
                let maxCalories;
                if (calorieGoal && actualMax > calorieGoal * 0.3) {
                  // If actual data is at least 30% of goal, include goal in range
                  maxCalories = Math.max(actualMax, calorieGoal);
                } else {
                  // Otherwise, just use actual data with some padding
                  maxCalories = actualMax > 0 ? actualMax * 1.2 : 100;
                }

                const yAxisMin = 0;
                let yAxisMax;

                // Smart scaling based on data size
                if (maxCalories <= 100) {
                  yAxisMax = Math.ceil(maxCalories / 10) * 10;
                } else if (maxCalories <= 500) {
                  yAxisMax = Math.ceil(maxCalories / 50) * 50;
                } else {
                  yAxisMax = Math.ceil(maxCalories / 500) * 500;
                }

                const yRange = yAxisMax - yAxisMin || 100;

                // Generate Y-axis labels
                const yLabels = [];
                const step = yRange / 4;
                for (let i = 0; i < 5; i++) {
                  yLabels.push(Math.round(yAxisMax - (step * i)));
                }

                // Calculate points for the chart
                const chartHeight = 160;
                const chartWidth = 780;
                const pointSpacing = chartWidth / (calorieData.length - 1);

                const points = calorieData.map((d, i) => {
                  const x = i * pointSpacing;
                  const y = chartHeight - ((d.calories - yAxisMin) / yRange) * chartHeight;
                  return { x, y, calories: d.calories };
                });

                const polylinePoints = points.map(p => `${p.x},${p.y}`).join(' ');
                const polygonPoints = `0,${chartHeight} ${polylinePoints} ${chartWidth},${chartHeight}`;

                // Calculate goal line position if exists
                let goalY = null;
                if (calorieGoal) {
                  goalY = chartHeight - ((calorieGoal - yAxisMin) / yRange) * chartHeight;
                }

                return (
                  <>
                    <div className="absolute left-0 top-0 bottom-0 flex flex-col justify-between text-xs text-muted-foreground pr-2 py-2">
                      {yLabels.map(n => <span key={n}>{n}</span>)}
                    </div>
                    <div className="ml-12 h-full border-l border-border border-b relative">
                      <div className="absolute inset-0 flex flex-col justify-between">
                        {[...Array(4)].map((_, i) => (
                          <div key={i} className="border-b border-border/40"></div>
                        ))}
                      </div>
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
                        <polygon
                          points={polygonPoints}
                          fill="url(#calGrad)"
                        />
                        <polyline
                          points={polylinePoints}
                          fill="none"
                          stroke="#F24E1E"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          vectorEffect="non-scaling-stroke"
                        />
                      </svg>
                      <div className="absolute -bottom-6 left-0 right-0 flex justify-between text-xs text-muted-foreground">
                        {calorieData.map((d, i) => {
                          const date = new Date(d.date);
                          const day = date.getDate().toString().padStart(2, '0');
                          return <span key={i}>{day}</span>;
                        })}
                      </div>
                    </div>
                  </>
                );
              })()
            ) : (
              <div className="flex items-center justify-center h-full text-muted-foreground text-sm">Nema podataka</div>
            )}
          </div>
        </div>
      </div>

      {/* Exercise Progress Table */}
      <div>
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="border-b border-border pb-3 mb-4">
            <h3
              className="text-base font-bold text-foreground uppercase tracking-wide"
              style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
            >
              Napredak po vježbama · Maksimalno opterećenje
            </h3>
          </div>

          <div className="border border-border rounded-lg overflow-hidden">
            {/* Table Header */}
            <div className="grid grid-cols-5 border-b border-border bg-secondary">
              {['Vježba','Početak','Trenutno','Napredak','Procenat'].map((h, i) => (
                <div key={h} className={`p-3 ${i < 4 ? 'border-r border-border' : ''}`}>
                  <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">{h}</span>
                </div>
              ))}
            </div>

            {exerciseProgress.length > 0 ? (
              exerciseProgress.map((row, i, arr) => (
                <div
                  key={row.name}
                  className={`grid grid-cols-5 hover:bg-secondary/50 transition-colors ${i < arr.length - 1 ? 'border-b border-border' : ''}`}
                >
                  <div className="p-3 border-r border-border">
                    <span className="text-sm text-foreground font-medium">{row.name}</span>
                  </div>
                  <div className="p-3 border-r border-border text-center">
                    <span className="text-sm text-muted-foreground">{row.start}</span>
                  </div>
                  <div className="p-3 border-r border-border text-center">
                    <span className="text-sm font-semibold text-foreground">{row.current}</span>
                  </div>
                  <div className="p-3 border-r border-border text-center">
                    <span className="text-sm font-semibold text-emerald-400">{row.gain}</span>
                  </div>
                  <div className="p-3 text-center">
                    <div className="flex items-center justify-center gap-2">
                      <div className="bg-secondary rounded-full h-2 w-20 overflow-hidden">
                        <div
                          className="bg-primary h-full rounded-full"
                          style={{ width: `${Math.min(row.pct, 100)}%` }}
                        ></div>
                      </div>
                      <span className="text-xs font-semibold text-primary">+{row.pct}%</span>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="p-8 text-center text-muted-foreground text-sm">Nema podataka o napretku vježbi</div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
