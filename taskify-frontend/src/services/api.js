import axios from 'axios';

// Base API configuration
// Use environment variable for API URL, fallback to localhost for development
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid - clear storage and redirect
      localStorage.removeItem('token');
      localStorage.removeItem('email');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ==================== Auth API ====================
export const authAPI = {
  register: (email, password) => 
    api.post('/auth/register', { email, password }),
  
  login: (email, password) => 
    api.post('/auth/login', { email, password }),

  logout: () =>
    api.post('/auth/logout'),
};

// ==================== Tasks API ====================
export const tasksAPI = {
  getAll: () => 
    api.get('/tasks'),
  
  getById: (id) => 
    api.get(`/tasks/${id}`),
  
  create: (task) => 
    api.post('/tasks', task),
  
  update: (id, task) => 
    api.put(`/tasks/${id}`, task),
  
  delete: (id) => 
    api.delete(`/tasks/${id}`),
};

// ==================== Appointments API ====================
export const appointmentsAPI = {
  getAll: () => 
    api.get('/appointments'),
  
  getById: (id) => 
    api.get(`/appointments/${id}`),
  
  create: (appointment) => 
    api.post('/appointments', appointment),
  
  update: (id, appointment) => 
    api.put(`/appointments/${id}`, appointment),
  
  delete: (id) => 
    api.delete(`/appointments/${id}`),
};

export default api;
