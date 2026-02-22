import axios from 'axios';

const AUTH_NOTICE_KEY = 'auth.notice';

function getAuthNotice(status?: number): string | null {
  if (status === 401) return 'Sua sessão expirou. Faça login novamente para continuar.';
  if (status === 403) return 'Você não tem permissão para acessar este recurso com a conta atual.';
  return null;
}

// Create axios instance with default config
const api = axios.create({
  // Default to same-origin so it works behind a reverse proxy (nginx) and with Vite proxy in dev.
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status as number | undefined;

    if (status === 401 || status === 403) {
      const notice = getAuthNotice(status);
      if (notice) {
        sessionStorage.setItem(AUTH_NOTICE_KEY, notice);
      }

      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.dispatchEvent(
        new CustomEvent('auth:session-invalid', {
          detail: { status },
        }),
      );
    }

    return Promise.reject(error);
  }
);

export default api;
