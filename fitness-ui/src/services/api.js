const API_BASE_URL = 'http://localhost:8080';

const getAuthHeader = () => {
  const token = localStorage.getItem('token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
};

export const api = {
  async getExercises(page = 0, size = 24, search = '', categoryId = null) {
    try {
      const params = new URLSearchParams({ page, size });

      if (search) {
        // Use search endpoint when search term is provided
        const response = await fetch(`${API_BASE_URL}/workouts/exercises/search?name=${encodeURIComponent(search)}&page=${page}&size=${size}`, {
          headers: getAuthHeader(),
        });
        if (!response.ok) {
          throw new Error('Failed to search exercises');
        }
        const text = await response.text();
        if (!text) {
          return { content: [], totalPages: 0, totalElements: 0 };
        }
        const data = JSON.parse(text);
        return data;
      } else {
        // Use regular endpoint for category filter or no filter
        if (categoryId) params.append('categoryId', categoryId);
        const response = await fetch(`${API_BASE_URL}/workouts/exercises?${params.toString()}`, {
          headers: getAuthHeader(),
        });
        if (!response.ok) {
          throw new Error('Failed to fetch exercises');
        }
        const text = await response.text();
        if (!text) {
          return { content: [], totalPages: 0, totalElements: 0 };
        }
        const data = JSON.parse(text);
        return data;
      }
    } catch (error) {
      console.error('Error fetching exercises:', error);
      throw error;
    }
  },

  async getExerciseCategories() {
    try {
      const response = await fetch(`${API_BASE_URL}/workouts/exercise-categories`, {
        headers: getAuthHeader(),
      });
      if (!response.ok) {
        throw new Error('Failed to fetch exercise categories');
      }
      const text = await response.text();
      if (!text) {
        return [];
      }
      return JSON.parse(text);
    } catch (error) {
      console.error('Error fetching exercise categories:', error);
      throw error;
    }
  },

  async getExerciseById(id) {
    try {
      const response = await fetch(`${API_BASE_URL}/workouts/exercises/${id}`, {
        headers: getAuthHeader(),
      });
      if (!response.ok) {
        throw new Error('Failed to fetch exercise');
      }
      const text = await response.text();
      if (!text) {
        return null;
      }
      return JSON.parse(text);
    } catch (error) {
      console.error('Error fetching exercise:', error);
      throw error;
    }
  },
};
