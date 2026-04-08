import axios, { AxiosError } from 'axios';

const AUTH_NOTICE_KEY = 'auth.notice';
const TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_KEY = 'user';

// Create axios instance with default config
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

function getAuthNotice(status?: number): string | null {
  if (status === 401) return 'Sua sessão expirou. Faça login novamente para continuar.';
  if (status === 403) return 'Você não tem permissão para acessar este recurso com a conta atual.';
  return null;
}

// Store tokens
export function setTokens(accessToken: string, refreshToken: string) {
  localStorage.setItem(TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

export function getAccessToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function clearTokens() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function setUser(user: object) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function getUser(): object | null {
  const userStr = localStorage.getItem(USER_KEY);
  return userStr ? JSON.parse(userStr) : null;
}

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle errors and token refresh
let isRefreshing = false;
let failedQueue: Array<{ resolve: (value: unknown) => void; reject: (reason?: unknown) => void }> = [];

function processQueue(error: AxiosError | null, token: string | null = null) {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && originalRequest && !originalRequest.url.includes('/auth/')) {
      if (!isRefreshing) {
        isRefreshing = true;
        const refreshToken = getRefreshToken();

        try {
          const response = await axios.post(`${import.meta.env.VITE_API_URL || '/api/v1'}/auth/refresh`, {
            refreshToken,
          });

          const { accessToken, refreshToken: newRefreshToken } = response.data;
          setTokens(accessToken, newRefreshToken);
          processQueue(null, accessToken);

          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
        } catch (refreshError) {
          processQueue(refreshError as AxiosError, null);
          
          // Clear tokens and redirect to login
          clearTokens();
          window.dispatchEvent(
            new CustomEvent('auth:session-invalid', {
              detail: { status: 401 },
            }),
          );
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      }

      // Queue the request while refreshing
      return new Promise((resolve, reject) => {
        failedQueue.push({
          resolve: (value) => {
            if (originalRequest && originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${value}`;
            }
            resolve(api(originalRequest!));
          },
          reject: (reason) => reject(reason),
        });
      });
    }

    const status = error.response?.status;
    if (status === 401 || status === 403) {
      const notice = getAuthNotice(status);
      if (notice) {
        sessionStorage.setItem(AUTH_NOTICE_KEY, notice);
      }

      clearTokens();
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
