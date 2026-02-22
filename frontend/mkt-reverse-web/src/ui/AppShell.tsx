import React from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { clsx } from 'clsx'

import { SettingsDock } from '@/ui/SettingsDock'

export function AppShell() {
  return (
    <div className="shell">
      <header className="top">
        <div className="brand">
          <div className="mark" aria-hidden="true">
            MR
          </div>
          <div className="brandText">
            <div className="brandName">mkt-reverse</div>
            <div className="brandTag">opportunities → proposals → acceptance</div>
          </div>
        </div>

        <nav className="nav" aria-label="Primary">
          <NavLink
            to="/directory/opportunities"
            className={({ isActive }) => clsx('navLink', isActive && 'active')}
            end
          >
            Diretório
          </NavLink>
          <NavLink
            to="/directory/suppliers"
            className={({ isActive }) => clsx('navLink', isActive && 'active')}
          >
            Fornecedores
          </NavLink>
          <NavLink
            to="/events/new"
            className={({ isActive }) => clsx('navLink', isActive && 'active')}
          >
            Criar pedido
          </NavLink>
          <NavLink
            to="/acordos"
            className={({ isActive }) => clsx('navLink', isActive && 'active')}
          >
            Acordos
          </NavLink>
          <NavLink
            to="/buyer/events"
            className={({ isActive }) => clsx('navLink', isActive && 'active')}
          >
            Buyer
          </NavLink>
          <NavLink
            to="/cadastros"
            className={({ isActive }) => clsx('navLink', isActive && 'active')}
          >
            Cadastros
          </NavLink>
        </nav>

        <SettingsDock />
      </header>

      <main className="main">
        <Outlet />
      </main>

      <footer className="foot">
        <span className="mono">local-only</span>
        <span className="dot" aria-hidden="true" />
        <span className="muted">API state is the source of truth.</span>
      </footer>
    </div>
  )
}
