import api from './api';
import { phoneToMvpEmail } from '../lib/phone';

export interface User {
  id: string;
  name: string;
  email: string;
  role: 'buyer' | 'supplier';
  organizationId?: string;
  tenantId: string;
}

export interface AuthResponse {
  token: string;
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

    const { token, user } = response.data;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    return { token, user };
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

    const { token, user } = response.data;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    return { token, user };
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  },

  getCurrentUser(): User | null {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },
  
  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }
};
