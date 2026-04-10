import React from 'react'
import { createBrowserRouter } from 'react-router-dom'

import { AppShell } from '@/ui/AppShell'
import { AgreementsPage } from '@/ui/pages/AgreementsPage'
import { BuyerEventsPage } from '@/ui/pages/BuyerEventsPage'
import { CadastrosPage } from '@/ui/pages/CadastrosPage'
import { CreateEventPage } from '@/ui/pages/CreateEventPage'
import { EventDetailPage } from '@/ui/pages/EventDetailPage'
import { OpportunitiesPage } from '@/ui/pages/OpportunitiesPage'
import { SupplierDirectoryPage } from '@/ui/pages/SupplierDirectoryPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppShell />,
    children: [
      { index: true, element: <OpportunitiesPage /> },
      { path: '/directory/opportunities', element: <OpportunitiesPage /> },
      { path: '/directory/suppliers', element: <SupplierDirectoryPage /> },
      { path: '/buyer/events', element: <BuyerEventsPage /> },
      { path: '/cadastros', element: <CadastrosPage /> },
      { path: '/acordos', element: <AgreementsPage /> },
      { path: '/events/new', element: <CreateEventPage /> },
      { path: '/events/:eventId', element: <EventDetailPage /> },
    ],
  },
])
