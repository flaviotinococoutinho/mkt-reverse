import api, { setTokens, clearTokens, setUser, getAccessToken } from './api';
import { phoneToMvpEmail } from '../lib/phone';

export interface User {
  id: string;
  name: string;
  email: string;
  role: 'buyer' | 'supplier';
  tenantId: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface LoginCredentials {
  identifier: string; // telefone (WhatsApp)
  password?: string;
}

export interface RegisterData {
  name: string;
  phone: string;
  password: string;
  role: 'buyer' | 'supplier';
  documentNumber: string;
  documentType: 'CPF' | 'CNPJ';
}

export const authService = {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const identifier = (credentials.identifier || '').trim();
    const email = phoneToMvpEmail(identifier);

    const response = await api.post<AuthResponse>('/auth/login', {
      email,
      password: credentials.password,
    });

    const { accessToken, refreshToken, user } = response.data;
    setTokens(accessToken, refreshToken);
    setUser(user);
    return { accessToken, refreshToken, user };
  },

  async register(data: RegisterData): Promise<AuthResponse> {
    const cleanedName = (data.name || '').trim();
    const [firstName, ...rest] = cleanedName.split(/\s+/);
    const lastName = rest.length > 0 ? rest.join(' ') : 'MVP';
    const email = phoneToMvpEmail(data.phone);

    const response = await api.post<AuthResponse>('/auth/register', {
      email,
      password: data.password,
      firstName,
      lastName,
      displayName: cleanedName,
      documentNumber: data.documentNumber,
      documentType: data.documentType,
      userType: data.role === 'supplier' ? 'SUPPLIER' : 'BUYER',
    });

    const { accessToken, refreshToken, user } = response.data;
    setTokens(accessToken, refreshToken);
    setUser(user);
    return { accessToken, refreshToken, user };
  },

  async logout() {
    try {
      const token = getAccessToken();
      if (token) {
        await api.post('/auth/logout', {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
      }
    } catch (error) {
      // Ignore logout errors - just clear local state
      console.warn('Logout API call failed:', error);
    } finally {
      clearTokens();
      window.location.href = '/login';
    }
  },

  getCurrentUser(): User | null {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated(): boolean {
    return !!getAccessToken();
  }
};
