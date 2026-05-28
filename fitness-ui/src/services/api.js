const API_BASE = {
  auth: 'http://localhost:8080/auth',
  user: 'http://localhost:8080/users',
  nutrition: 'http://localhost:8080/nutrition',
  workout: 'http://localhost:8080/workouts',
  notification: 'http://localhost:8080/notifications',
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
  if (response.status === 204) return null;
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
  // ── AUTH SERVICE (/auth) ──────────────────────────────────
  getRoles: () => request(`${API_BASE.auth}/roles`),
  getRoleById: (id) => request(`${API_BASE.auth}/roles/${id}`),
  createRole: (data) => requestJson(`${API_BASE.auth}/roles`, 'POST', data),
  updateRole: (id, data) => requestJson(`${API_BASE.auth}/roles/${id}`, 'PUT', data),
  deleteRole: (id) => request(`${API_BASE.auth}/roles/${id}`, { method: 'DELETE' }),

  getUsers: () => request(`${API_BASE.auth}/users`),
  getUserById: (id) => request(`${API_BASE.auth}/users/${id}`),
  createUser: (data) => requestJson(`${API_BASE.auth}/users`, 'POST', data),
  updateUser: (id, data) => requestJson(`${API_BASE.auth}/users/${id}`, 'PUT', data),
  updateUserProfile: (id, data) => requestJson(`${API_BASE.auth}/users/${id}/profile`, 'PATCH', data),
  deleteUser: (id) => request(`${API_BASE.auth}/users/${id}`, { method: 'DELETE' }),

  // ── USER SERVICE (/users) ──────────────────────────────────
  getFitnessGoals: () => request(`${API_BASE.user}/fitness-goals`),
  getFitnessGoalById: (id) => request(`${API_BASE.user}/fitness-goals/${id}`),
  getFitnessGoalsByUserId: (userId) => request(`${API_BASE.user}/fitness-goals/user/${userId}`),
  getActiveFitnessGoal: (userId) => request(`${API_BASE.user}/fitness-goals/user/${userId}/active`),
  createFitnessGoal: (data) => requestJson(`${API_BASE.user}/fitness-goals`, 'POST', data),
  updateFitnessGoal: (id, data) => requestJson(`${API_BASE.user}/fitness-goals/${id}`, 'PUT', data),
  deleteFitnessGoal: (id) => request(`${API_BASE.user}/fitness-goals/${id}`, { method: 'DELETE' }),

  getTrainerClients: () => request(`${API_BASE.user}/trainer-clients`),
  getTrainerClientById: (id) => request(`${API_BASE.user}/trainer-clients/${id}`),
  createTrainerClient: (data) => requestJson(`${API_BASE.user}/trainer-clients`, 'POST', data),
  updateTrainerClient: (id, data) => requestJson(`${API_BASE.user}/trainer-clients/${id}`, 'PUT', data),
  deleteTrainerClient: (id) => request(`${API_BASE.user}/trainer-clients/${id}`, { method: 'DELETE' }),

  // ── NOTIFICATION SERVICE (/notifications) ──────────────────
  getNotifications: () => request(`${API_BASE.notification}/notifications`),
  getNotificationById: (id) => request(`${API_BASE.notification}/notifications/${id}`),
  createNotification: (data) => requestJson(`${API_BASE.notification}/notifications`, 'POST', data),
  updateNotification: (id, data) => requestJson(`${API_BASE.notification}/notifications/${id}`, 'PUT', data),
  deleteNotification: (id) => request(`${API_BASE.notification}/notifications/${id}`, { method: 'DELETE' }),

  // ── NUTRITION SERVICE (/nutrition) ─────────────────────────
  getMealLogs: () => request(`${API_BASE.nutrition}/meal-logs`),
  getMealLogById: (id) => request(`${API_BASE.nutrition}/meal-logs/${id}`),
  createMealLog: (data) => requestJson(`${API_BASE.nutrition}/meal-logs`, 'POST', data),
  updateMealLog: (id, data) => requestJson(`${API_BASE.nutrition}/meal-logs/${id}`, 'PUT', data),
  deleteMealLog: (id) => request(`${API_BASE.nutrition}/meal-logs/${id}`, { method: 'DELETE' }),

  getMealItems: () => request(`${API_BASE.nutrition}/meal-items`),
  getMealItemById: (id) => request(`${API_BASE.nutrition}/meal-items/${id}`),
  createMealItem: (data) => requestJson(`${API_BASE.nutrition}/meal-items`, 'POST', data),
  updateMealItem: (id, data) => requestJson(`${API_BASE.nutrition}/meal-items/${id}`, 'PUT', data),
  deleteMealItem: (id) => request(`${API_BASE.nutrition}/meal-items/${id}`, { method: 'DELETE' }),

  getProgressEntries: () => request(`${API_BASE.nutrition}/progress-entries`),
  getProgressEntryById: (id) => request(`${API_BASE.nutrition}/progress-entries/${id}`),
  createProgressEntry: (data) => requestJson(`${API_BASE.nutrition}/progress-entries`, 'POST', data),
  updateProgressEntry: (id, data) => requestJson(`${API_BASE.nutrition}/progress-entries/${id}`, 'PUT', data),
  deleteProgressEntry: (id) => request(`${API_BASE.nutrition}/progress-entries/${id}`, { method: 'DELETE' }),

  // ── WORKOUT SERVICE (/workouts) ───────────────────────────────
  getExercises: async (page = 0, size = 9, search = '', categoryId = null) => {
    const base = API_BASE.workout;
    if (search) {
      return request(`${base}/exercises/search?name=${encodeURIComponent(search)}&page=${page}&size=${size}`);
    }
    const params = new URLSearchParams({ page, size });
    if (categoryId) params.append('categoryId', categoryId);
    return request(`${base}/exercises?${params}`);
  },
  getExerciseById: (id) => request(`${API_BASE.workout}/exercises/${id}`),
  createExercise: (data) => requestJson(`${API_BASE.workout}/exercises`, 'POST', data),
  updateExercise: (id, data) => requestJson(`${API_BASE.workout}/exercises/${id}`, 'PUT', data),
  deleteExercise: (id) => request(`${API_BASE.workout}/exercises/${id}`, { method: 'DELETE' }),

  getExerciseCategories: () => request(`${API_BASE.workout}/exercise-categories`),
  createExerciseCategory: (data) => requestJson(`${API_BASE.workout}/exercise-categories`, 'POST', data),
  updateExerciseCategory: (id, data) => requestJson(`${API_BASE.workout}/exercise-categories/${id}`, 'PUT', data),
  deleteExerciseCategory: (id) => request(`${API_BASE.workout}/exercise-categories/${id}`, { method: 'DELETE' }),

  getExerciseCategoryMaps: () => request(`${API_BASE.workout}/exercise-category-maps`),
  createExerciseCategoryMap: (data) => requestJson(`${API_BASE.workout}/exercise-category-maps`, 'POST', data),
  deleteExerciseCategoryMap: (id) => request(`${API_BASE.workout}/exercise-category-maps/${id}`, { method: 'DELETE' }),

  getWorkoutPlans: (userId) => {
    if (!userId) return request(`${API_BASE.workout}/workout-plans`);
    if (userId === 'undefined' || userId === 'null') return Promise.resolve([]);
    return request(`${API_BASE.workout}/workout-plans/user/${userId}`);
  },
  createWorkoutPlan: (data) => requestJson(`${API_BASE.workout}/workout-plans`, 'POST', data),
  updateWorkoutPlan: (id, data) => requestJson(`${API_BASE.workout}/workout-plans/${id}`, 'PUT', data),
  deleteWorkoutPlan: (id) => request(`${API_BASE.workout}/workout-plans/${id}`, { method: 'DELETE' }),

  getWorkoutDays: () => request(`${API_BASE.workout}/workout-days`),
  createWorkoutDay: (data) => requestJson(`${API_BASE.workout}/workout-days`, 'POST', data),
  updateWorkoutDay: (id, data) => requestJson(`${API_BASE.workout}/workout-days/${id}`, 'PUT', data),
  deleteWorkoutDay: (id) => request(`${API_BASE.workout}/workout-days/${id}`, { method: 'DELETE' }),

  getWorkoutExercises: () => request(`${API_BASE.workout}/workout-exercises`),
  createWorkoutExercise: (data) => requestJson(`${API_BASE.workout}/workout-exercises`, 'POST', data),
  updateWorkoutExercise: (id, data) => requestJson(`${API_BASE.workout}/workout-exercises/${id}`, 'PUT', data),
  deleteWorkoutExercise: (id) => request(`${API_BASE.workout}/workout-exercises/${id}`, { method: 'DELETE' }),

  getCompletedWorkouts: () => request(`${API_BASE.workout}/completed-workouts`),
  createCompletedWorkout: (data) => requestJson(`${API_BASE.workout}/completed-workouts`, 'POST', data),
  updateCompletedWorkout: (id, data) => requestJson(`${API_BASE.workout}/completed-workouts/${id}`, 'PUT', data),
  deleteCompletedWorkout: (id) => request(`${API_BASE.workout}/completed-workouts/${id}`, { method: 'DELETE' }),

  getCompletedExercises: () => request(`${API_BASE.workout}/completed-exercises`),
  createCompletedExercise: (data) => requestJson(`${API_BASE.workout}/completed-exercises`, 'POST', data),
  updateCompletedExercise: (id, data) => requestJson(`${API_BASE.workout}/completed-exercises/${id}`, 'PUT', data),
  deleteCompletedExercise: (id) => request(`${API_BASE.workout}/completed-exercises/${id}`, { method: 'DELETE' }),
};