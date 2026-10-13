import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const dashboardApi = {
  getStats: () => api.get('/dashboard/stats').then(res => res.data),
};

export const activityApi = {
  record: (data) => api.post('/activity/record', data).then(res => res.data),
  getCurrent: () => api.get('/activity/current').then(res => res.data),
  getStatus: () => api.get('/activity/status').then(res => res.data),
};

export const focusApi = {
  getActive: () => api.get('/focus-sessions/active').then(res => res.data),
  start: (params) => api.post('/focus-sessions/start', params).then(res => res.data),
  pause: () => api.post('/focus-sessions/pause').then(res => res.data),
  resume: () => api.post('/focus-sessions/resume').then(res => res.data),
  stop: () => api.post('/focus-sessions/stop').then(res => res.data),
  skipBreak: () => api.post('/focus-sessions/skip-break').then(res => res.data),
  getHistory: () => api.get('/focus-sessions/history').then(res => res.data),
};

export const goalsApi = {
  getAll: () => api.get('/goals').then(res => res.data),
  create: (goal) => api.post('/goals', goal).then(res => res.data),
  update: (id, goal) => api.put(`/goals/${id}`, goal).then(res => res.data),
  delete: (id) => api.delete(`/goals/${id}`).then(res => res.data),
};

export const categoriesApi = {
  getAll: () => api.get('/categories').then(res => res.data),
  update: (id, data) => api.put(`/categories/${id}`, data).then(res => res.data),
  getApps: () => api.get('/categories/apps').then(res => res.data),
  assignApp: (applicationId, categoryId) => api.post('/categories/assign', { applicationId, categoryId }).then(res => res.data),
};

export const reportsApi = {
  getDaily: (date) => api.get('/reports/daily', { params: { date } }).then(res => res.data),
  getWeekly: (weekOffset) => api.get('/reports/weekly', { params: { weekOffset } }).then(res => res.data),
  getMonthly: (monthOffset) => api.get('/reports/monthly', { params: { monthOffset } }).then(res => res.data),
};

export const exportsApi = {
  downloadDailyCsv: (date) => window.open(`/api/exports/csv/daily${date ? `?date=${date}` : ''}`, '_blank'),
  downloadWeeklyCsv: (weekOffset) => window.open(`/api/exports/csv/weekly?weekOffset=${weekOffset || 0}`, '_blank'),
  downloadDailyPdf: (date) => window.open(`/api/exports/pdf/daily${date ? `?date=${date}` : ''}`, '_blank'),
  downloadWeeklyPdf: (weekOffset) => window.open(`/api/exports/pdf/weekly?weekOffset=${weekOffset || 0}`, '_blank'),
};

export const settingsApi = {
  getAll: () => api.get('/settings').then(res => res.data),
  updateAll: (data) => api.put('/settings', data).then(res => res.data),
  updateSingle: (key, value) => api.put(`/settings/${key}`, { value }).then(res => res.data),
};

export const appBlockerApi = {
  getRules: () => api.get('/app-blocker/rules').then(res => res.data),
  addRule: (data) => api.post('/app-blocker/rules', data).then(res => res.data),
  removeRule: (id) => api.delete(`/app-blocker/rules/${id}`).then(res => res.data),
  getStatus: () => api.get('/app-blocker/status').then(res => res.data),
};

export default api;
