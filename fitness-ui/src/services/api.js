const API_BASE = {
  auth: 'http://localhost:8084',
  user: 'http://localhost:8081',
  nutrition: 'http://localhost:8082',
  workout: 'http://localhost:8083',
  notification: 'http://localhost:8085',
};

const getAuthHeader = () => {
  const token = localStorage.getItem('token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
};

const jsonHeaders = () => ({
  'Content-Type': 'application/json',
  ...getAuthHeader(),
});

async function request(url, options = {}) {
  const response = await fetch(url, { headers: getAuthHeader(), ...options });
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

async function requestJson(url, method, body) {
  const response = await fetch(url, {
    method,
    headers: jsonHeaders(),
    body: JSON.stringify(body),
  });
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  // ── AUTH SERVICE (8084) ──────────────────────────────────
  getRoles: () => request(`${API_BASE.auth}/api/roles`),
  getRoleById: (id) => request(`${API_BASE.auth}/api/roles/${id}`),
  createRole: (data) => requestJson(`${API_BASE.auth}/api/roles`, 'POST', data),
  updateRole: (id, data) => requestJson(`${API_BASE.auth}/api/roles/${id}`, 'PUT', data),
  deleteRole: (id) => request(`${API_BASE.auth}/api/roles/${id}`, { method: 'DELETE' }),

  getUsers: () => request(`${API_BASE.auth}/api/users`),
  getUserById: (id) => request(`${API_BASE.auth}/api/users/${id}`),
  createUser: (data) => requestJson(`${API_BASE.auth}/api/users`, 'POST', data),
  updateUser: (id, data) => requestJson(`${API_BASE.auth}/api/users/${id}`, 'PUT', data),
  deleteUser: (id) => request(`${API_BASE.auth}/api/users/${id}`, { method: 'DELETE' }),

  // ── USER SERVICE (8081) ──────────────────────────────────
  getFitnessGoals: () => request(`${API_BASE.user}/api/fitness-goals`),
  getFitnessGoalById: (id) => request(`${API_BASE.user}/api/fitness-goals/${id}`),
  createFitnessGoal: (data) => requestJson(`${API_BASE.user}/api/fitness-goals`, 'POST', data),
  updateFitnessGoal: (id, data) => requestJson(`${API_BASE.user}/api/fitness-goals/${id}`, 'PUT', data),
  deleteFitnessGoal: (id) => request(`${API_BASE.user}/api/fitness-goals/${id}`, { method: 'DELETE' }),

  getTrainerClients: () => request(`${API_BASE.user}/api/trainer-clients`),
  getTrainerClientById: (id) => request(`${API_BASE.user}/api/trainer-clients/${id}`),
  createTrainerClient: (data) => requestJson(`${API_BASE.user}/api/trainer-clients`, 'POST', data),
  updateTrainerClient: (id, data) => requestJson(`${API_BASE.user}/api/trainer-clients/${id}`, 'PUT', data),
  deleteTrainerClient: (id) => request(`${API_BASE.user}/api/trainer-clients/${id}`, { method: 'DELETE' }),

  // ── NOTIFICATION SERVICE (8085) ──────────────────────────
  getNotifications: () => request(`${API_BASE.notification}/api/notifications`),
  getNotificationById: (id) => request(`${API_BASE.notification}/api/notifications/${id}`),
  createNotification: (data) => requestJson(`${API_BASE.notification}/api/notifications`, 'POST', data),
  updateNotification: (id, data) => requestJson(`${API_BASE.notification}/api/notifications/${id}`, 'PUT', data),
  deleteNotification: (id) => request(`${API_BASE.notification}/api/notifications/${id}`, { method: 'DELETE' }),

  // ── NUTRITION SERVICE (8082) ─────────────────────────────
  getMealLogs: () => request(`${API_BASE.nutrition}/api/meal-logs`),
  getMealLogById: (id) => request(`${API_BASE.nutrition}/api/meal-logs/${id}`),
  createMealLog: (data) => requestJson(`${API_BASE.nutrition}/api/meal-logs`, 'POST', data),
  updateMealLog: (id, data) => requestJson(`${API_BASE.nutrition}/api/meal-logs/${id}`, 'PUT', data),
  deleteMealLog: (id) => request(`${API_BASE.nutrition}/api/meal-logs/${id}`, { method: 'DELETE' }),

  getMealItems: () => request(`${API_BASE.nutrition}/api/meal-items`),
  getMealItemById: (id) => request(`${API_BASE.nutrition}/api/meal-items/${id}`),
  createMealItem: (data) => requestJson(`${API_BASE.nutrition}/api/meal-items`, 'POST', data),
  updateMealItem: (id, data) => requestJson(`${API_BASE.nutrition}/api/meal-items/${id}`, 'PUT', data),
  deleteMealItem: (id) => request(`${API_BASE.nutrition}/api/meal-items/${id}`, { method: 'DELETE' }),

  getProgressEntries: () => request(`${API_BASE.nutrition}/api/progress-entries`),
  getProgressEntryById: (id) => request(`${API_BASE.nutrition}/api/progress-entries/${id}`),
  createProgressEntry: (data) => requestJson(`${API_BASE.nutrition}/api/progress-entries`, 'POST', data),
  updateProgressEntry: (id, data) => requestJson(`${API_BASE.nutrition}/api/progress-entries/${id}`, 'PUT', data),
  deleteProgressEntry: (id) => request(`${API_BASE.nutrition}/api/progress-entries/${id}`, { method: 'DELETE' }),

  // ── WORKOUT SERVICE (8083) ───────────────────────────────
  getExercises: async (page = 0, size = 9, search = '', categoryId = null) => {
    const base = API_BASE.workout;
    if (search) {
      return request(`${base}/api/exercises?name=${encodeURIComponent(search)}&page=${page}&size=${size}`);
    }
    const params = new URLSearchParams({ page, size });
    if (categoryId) params.append('categoryId', categoryId);
    return request(`${base}/api/exercises?${params}`);
  },
  getExerciseById: (id) => request(`${API_BASE.workout}/api/exercises/${id}`),
  createExercise: (data) => requestJson(`${API_BASE.workout}/api/exercises`, 'POST', data),
  updateExercise: (id, data) => requestJson(`${API_BASE.workout}/api/exercises/${id}`, 'PUT', data),
  deleteExercise: (id) => request(`${API_BASE.workout}/api/exercises/${id}`, { method: 'DELETE' }),

  getExerciseCategories: () => request(`${API_BASE.workout}/api/exercise-categories`),
  createExerciseCategory: (data) => requestJson(`${API_BASE.workout}/api/exercise-categories`, 'POST', data),
  updateExerciseCategory: (id, data) => requestJson(`${API_BASE.workout}/api/exercise-categories/${id}`, 'PUT', data),
  deleteExerciseCategory: (id) => request(`${API_BASE.workout}/api/exercise-categories/${id}`, { method: 'DELETE' }),

  getExerciseCategoryMaps: () => request(`${API_BASE.workout}/api/exercise-category-maps`),
  createExerciseCategoryMap: (data) => requestJson(`${API_BASE.workout}/api/exercise-category-maps`, 'POST', data),
  deleteExerciseCategoryMap: (id) => request(`${API_BASE.workout}/api/exercise-category-maps/${id}`, { method: 'DELETE' }),

  getWorkoutPlans: () => request(`${API_BASE.workout}/api/workout-plans`),
  createWorkoutPlan: (data) => requestJson(`${API_BASE.workout}/api/workout-plans`, 'POST', data),
  updateWorkoutPlan: (id, data) => requestJson(`${API_BASE.workout}/api/workout-plans/${id}`, 'PUT', data),
  deleteWorkoutPlan: (id) => request(`${API_BASE.workout}/api/workout-plans/${id}`, { method: 'DELETE' }),

  getWorkoutDays: () => request(`${API_BASE.workout}/api/workout-days`),
  createWorkoutDay: (data) => requestJson(`${API_BASE.workout}/api/workout-days`, 'POST', data),
  updateWorkoutDay: (id, data) => requestJson(`${API_BASE.workout}/api/workout-days/${id}`, 'PUT', data),
  deleteWorkoutDay: (id) => request(`${API_BASE.workout}/api/workout-days/${id}`, { method: 'DELETE' }),

  getWorkoutExercises: () => request(`${API_BASE.workout}/api/workout-exercises`),
  createWorkoutExercise: (data) => requestJson(`${API_BASE.workout}/api/workout-exercises`, 'POST', data),
  updateWorkoutExercise: (id, data) => requestJson(`${API_BASE.workout}/api/workout-exercises/${id}`, 'PUT', data),
  deleteWorkoutExercise: (id) => request(`${API_BASE.workout}/api/workout-exercises/${id}`, { method: 'DELETE' }),

  getCompletedWorkouts: () => request(`${API_BASE.workout}/api/completed-workouts`),
  createCompletedWorkout: (data) => requestJson(`${API_BASE.workout}/api/completed-workouts`, 'POST', data),
  updateCompletedWorkout: (id, data) => requestJson(`${API_BASE.workout}/api/completed-workouts/${id}`, 'PUT', data),
  deleteCompletedWorkout: (id) => request(`${API_BASE.workout}/api/completed-workouts/${id}`, { method: 'DELETE' }),

  getCompletedExercises: () => request(`${API_BASE.workout}/api/completed-exercises`),
  createCompletedExercise: (data) => requestJson(`${API_BASE.workout}/api/completed-exercises`, 'POST', data),
  updateCompletedExercise: (id, data) => requestJson(`${API_BASE.workout}/api/completed-exercises/${id}`, 'PUT', data),
  deleteCompletedExercise: (id) => request(`${API_BASE.workout}/api/completed-exercises/${id}`, { method: 'DELETE' }),
};
