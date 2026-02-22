import React, { useEffect, useState } from 'react';
import type { User } from '../services/authService';
import { authService } from '../services/authService';
import { AuthContext } from './auth-context';

function getInitialUser(): User | null {
  const storedUser = localStorage.getItem('user');
  const storedToken = localStorage.getItem('token');

  if (!storedUser || !storedToken) {
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
    };

    window.addEventListener('auth:session-invalid', handleSessionInvalid as EventListener);
    return () => {
      window.removeEventListener('auth:session-invalid', handleSessionInvalid as EventListener);
    };
  }, []);

  const login = (newUser: User, newToken: string) => {
    setUser(newUser);
    localStorage.setItem('user', JSON.stringify(newUser));
    localStorage.setItem('token', newToken);
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const isAuthenticated = !!user && !!localStorage.getItem('token');

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
};
