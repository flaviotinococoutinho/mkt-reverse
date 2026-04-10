import React, { useEffect, useState } from 'react';
import type { User } from '../services/authService';
import { authService } from '../services/authService';
import { AuthContext } from './auth-context';
import { getAccessToken, clearTokens } from '../services/api';

function getInitialUser(): User | null {
  const storedUser = localStorage.getItem('user');
  const token = getAccessToken();

  if (!storedUser || !token) {
    return null;
  }

  try {
    return JSON.parse(storedUser) as User;
  } catch {
    return null;
  }
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(getInitialUser);

  useEffect(() => {
    const handleSessionInvalid = () => {
      setUser(null);
      clearTokens();
    };

    window.addEventListener('auth:session-invalid', handleSessionInvalid as EventListener);
    return () => {
      window.removeEventListener('auth:session-invalid', handleSessionInvalid as EventListener);
    };
  }, []);

  const login = (newUser: User) => {
    setUser(newUser);
    localStorage.setItem('user', JSON.stringify(newUser));
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const isAuthenticated = !!user && authService.isAuthenticated();

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
};
