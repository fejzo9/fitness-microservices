import axios from 'axios';

const API_BASE = {
  auth: '/auth',
  user: '/users',
  nutrition: '/nutrition',
  workout: '/workouts',
  notification: '/notifications',
};

// ── Axios instanca ────────────────────────────────────────────
const axiosInstance = axios.create();

// Request interceptor — dodaje Bearer token na svaki zahtjev
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

// Sprječava višestruke istovremene refresh pozive
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Response interceptor — automatski refresh na 401
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refreshToken');

      // Nema refresh tokena — odjavi korisnika
      if (!refreshToken) {
        _logoutCallback?.();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // Čekaj dok se refresh ne završi, pa ponovi zahtjev
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers['Authorization'] = `Bearer ${token}`;
            return axiosInstance(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const { data } = await axios.post(`${API_BASE.auth}/refresh`, { refreshToken });
        const newToken = data.accessToken;
        localStorage.setItem('token', newToken);
        if (data.refreshToken) {
          localStorage.setItem('refreshToken', data.refreshToken);
        }
        axiosInstance.defaults.headers['Authorization'] = `Bearer ${newToken}`;
        processQueue(null, newToken);
        originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        _logoutCallback?.();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// Callback za odjavu koji postavlja AuthContext
let _logoutCallback = null;
export const setLogoutCallback = (fn) => {
  _logoutCallback = fn;
};

// ── Pomoćne funkcije ──────────────────────────────────────────
async function request(url, options = {}) {
  const response = await axiosInstance({ url, ...options });
  return response.data ?? null;
}

async function requestJson(url, method, body) {
  const response = await axiosInstance({ url, method, data: body });
  return response.data ?? null;
}

// ── API metode ────────────────────────────────────────────────
export const api = {
  // ── AUTH — login / register / refresh ────────────────────────
  login: (credentials) =>
    axiosInstance.post(`${API_BASE.auth}/login`, credentials).then((r) => r.data),
  register: (userData) =>
    axiosInstance.post(`${API_BASE.auth}/register`, userData).then((r) => r.data),
  refreshToken: (refreshToken) =>
    axiosInstance.post(`${API_BASE.auth}/refresh`, { refreshToken }).then((r) => r.data),

  // ── AUTH SERVICE (/auth) ──────────────────────────────────────
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

  getWeightHistoryByUserId: (userId) => request(`${API_BASE.auth}/weight-history/user/${userId}`),
  addWeightEntry: (data) => requestJson(`${API_BASE.auth}/weight-history`, 'POST', data),

  // ── USER SERVICE (/users) ─────────────────────────────────────
  getFitnessGoals: () => request(`${API_BASE.user}/fitness-goals`),
  getFitnessGoalById: (id) => request(`${API_BASE.user}/fitness-goals/${id}`),
  getFitnessGoalsByUserId: (userId) => request(`${API_BASE.user}/fitness-goals/user/${userId}`),
  getActiveFitnessGoal: (userId) => request(`${API_BASE.user}/fitness-goals/user/${userId}/active`),
  createFitnessGoal: (data) => requestJson(`${API_BASE.user}/fitness-goals`, 'POST', data),
  updateFitnessGoal: (id, data) => requestJson(`${API_BASE.user}/fitness-goals/${id}`, 'PUT', data),
  deleteFitnessGoal: (id) => request(`${API_BASE.user}/fitness-goals/${id}`, { method: 'DELETE' }),

  getTrainerClients: () => request(`${API_BASE.user}/trainer-clients`),
  getTrainerClientsByTrainerId: (trainerId) => request(`${API_BASE.user}/trainer-clients/trainer/${trainerId}`),
  getTrainerClientById: (id) => request(`${API_BASE.user}/trainer-clients/${id}`),
  createTrainerClient: (data) => requestJson(`${API_BASE.user}/trainer-clients`, 'POST', data),
  updateTrainerClient: (id, data) => requestJson(`${API_BASE.user}/trainer-clients/${id}`, 'PUT', data),
  deleteTrainerClient: (id) => request(`${API_BASE.user}/trainer-clients/${id}`, { method: 'DELETE' }),

  // ── NOTIFICATION SERVICE (/notifications) ─────────────────────
  getNotifications: () => request(`${API_BASE.notification}/notifications`),
  getNotificationById: (id) => request(`${API_BASE.notification}/notifications/${id}`),
  createNotification: (data) => requestJson(`${API_BASE.notification}/notifications`, 'POST', data),
  updateNotification: (id, data) => requestJson(`${API_BASE.notification}/notifications/${id}`, 'PUT', data),
  deleteNotification: (id) => request(`${API_BASE.notification}/notifications/${id}`, { method: 'DELETE' }),

  // ── NUTRITION SERVICE (/nutrition) ────────────────────────────
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

  getMealEntries: () => request(`${API_BASE.nutrition}/meal-entries`),
  getMealEntriesByUserAndDate: (userId, date) => request(`${API_BASE.nutrition}/meal-entries/user/${userId}/date/${date}`),
  getMealEntriesByUser: (userId) => request(`${API_BASE.nutrition}/meal-entries/user/${userId}`),
  getMealEntryById: (id) => request(`${API_BASE.nutrition}/meal-entries/${id}`),
  createMealEntry: (data) => requestJson(`${API_BASE.nutrition}/meal-entries`, 'POST', data),
  updateMealEntry: (id, data) => requestJson(`${API_BASE.nutrition}/meal-entries/${id}`, 'PUT', data),
  deleteMealEntry: (id) => request(`${API_BASE.nutrition}/meal-entries/${id}`, { method: 'DELETE' }),

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

  getWorkoutExercises: (userId = null, nextWeek = false) => {
    if (userId) return request(`${API_BASE.workout}/workout-exercises/user/${userId}?nextWeek=${nextWeek}`);
    return request(`${API_BASE.workout}/workout-exercises`);
  },
  getWorkoutHistory: (userId, page = 0, size = 10) => 
    request(`${API_BASE.workout}/workout-exercises/user/${userId}/history?page=${page}&size=${size}&sort=scheduledDate,desc`),
  getWorkoutExercisesByDay: (userId, day) => request(`${API_BASE.workout}/workout-exercises/user/${userId}/day/${day}`),
  getWorkoutStatistics: (userId) => request(`${API_BASE.workout}/workout-exercises/user/${userId}/statistics`),
  createWorkoutExercise: (data) => requestJson(`${API_BASE.workout}/workout-exercises`, 'POST', data),
  updateWorkoutExercise: (id, data) => requestJson(`${API_BASE.workout}/workout-exercises/${id}`, 'PUT', data),
  completeWorkoutExercise: (id) => request(`${API_BASE.workout}/workout-exercises/${id}/complete`, { method: 'PATCH' }),
  deleteWorkoutExercise: (id) => request(`${API_BASE.workout}/workout-exercises/${id}`, { method: 'DELETE' }),

  getLatestNotifications: (userId) => request(`${API_BASE.workout}/notifications/latest/${userId}`),

  getCompletedWorkouts: () => request(`${API_BASE.workout}/completed-workouts`),
  getCompletedWorkoutsByUserId: (userId) => request(`${API_BASE.workout}/workout-exercises/user/${userId}/completed`),
  createCompletedWorkout: (data) => requestJson(`${API_BASE.workout}/completed-workouts`, 'POST', data),
  updateCompletedWorkout: (id, data) => requestJson(`${API_BASE.workout}/completed-workouts/${id}`, 'PUT', data),
  deleteCompletedWorkout: (id) => request(`${API_BASE.workout}/completed-workouts/${id}`, { method: 'DELETE' }),

  getCompletedExercises: () => request(`${API_BASE.workout}/completed-exercises`),
  getCompletedExercisesByUserId: (userId) => request(`${API_BASE.workout}/workout-exercises/user/${userId}/completed`),
  getCompletedExercisesByExerciseId: (exerciseId) => request(`${API_BASE.workout}/completed-exercises/exercise/${exerciseId}`),
  createCompletedExercise: (data) => requestJson(`${API_BASE.workout}/completed-exercises`, 'POST', data),
  updateCompletedExercise: (id, data) => requestJson(`${API_BASE.workout}/completed-exercises/${id}`, 'PUT', data),
  deleteCompletedExercise: (id) => request(`${API_BASE.workout}/completed-exercises/${id}`, { method: 'DELETE' }),
};
