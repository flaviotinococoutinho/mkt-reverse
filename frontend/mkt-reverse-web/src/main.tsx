import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router-dom'

import { router } from '@/router'
import '@/theme/global.css'
import '@/ui/styles.css'

// Mock User for MVP Development (aligns with backend test defaults)
import { AuthProvider, User } from '@/ui/context/AuthContext'

const MOCK_USER: User = {
  tenantId: 'tenant-1',
  buyerOrganizationId: 'org-1',
  buyerContactName: 'Comprador Mockado',
  buyerContactPhone: '+5527999999999',
  roles: ['BUYER'],
}

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 30_000,
    },
  },
})

function App() {
  return (
    <AuthProvider user={MOCK_USER}>
      <RouterProvider router={router} />
    </AuthProvider>
  )
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>,
)
