import { createContext, useContext } from 'react'

export interface User {
  tenantId: string
  buyerOrganizationId: string
  buyerContactName: string
  buyerContactPhone: string
  roles: Array<'BUYER' | 'SUPPLIER' | 'ADMIN'>
}

interface AuthContextType {
  user: User | null
  setUser: (user: User | null) => void
  login: (user: User | null) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export function AuthProvider({ children, user }: { children: React.ReactNode; user: User | null }) {
  // In a real app, this would validate tokens with the backend.
  // For this MVP, we accept the user object provided by the parent (e.g., from a login screen).
  const login = (incomingUser: User | null) => {
    // Mock login logic
  }

  const logout = () => {
    // Mock logout logic
  }

  return (
    <AuthContext.Provider value={{ user, setUser: login, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
