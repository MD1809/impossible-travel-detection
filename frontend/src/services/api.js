import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Response interceptor để xử lý lỗi tập trung
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response && error.response.data) {
      return Promise.reject(error.response.data);
    }
    return Promise.reject({
      success: false,
      message: error.message || 'Không thể kết nối đến máy chủ Backend'
    });
  }
);

export const DashboardApi = {
  // 1. Thống kê KPI tổng hợp
  getSummary: () => api.get('/dashboard/summary'),

  // 2. Danh sách sự kiện an ninh phân trang và lọc
  getSecurityEvents: (page = 0, size = 10, severity = null) => {
    const params = { page, size };
    if (severity) params.severity = severity;
    return api.get('/security-events', { params });
  },

  // 3. Chi tiết một sự kiện an ninh
  getSecurityEventDetail: (id) => api.get(`/security-events/${id}`),

  // 4. Lịch sử đăng nhập
  getLoginHistory: (limit = 50) => api.get('/login-history', { params: { limit } }),

  // 5. Các phiên đang mở
  getActiveSessions: () => api.get('/sessions/active'),

  // 6. Mô phỏng đăng nhập để test trực tiếp
  login: (username, password, ipAddress, loginTime) => {
    const payload = { username, password, ipAddress };
    if (loginTime) payload.loginTime = loginTime;
    return api.post('/auth/login', payload);
  },

  // 7. Đăng ký tài khoản
  register: (username, password, fullName) => api.post('/auth/register', { username, password, fullName }),

  // 8. Đăng xuất
  logout: (token) => api.post('/auth/logout', {}, {
    headers: { Authorization: `Bearer ${token}` }
  }),

  // 9. Đăng xuất toàn bộ phiên của tài khoản
  logoutAll: (username) => api.post('/auth/logout-all', { username })
};

export default api;
