import React from 'react'

export function Panel({ children }: { children: React.ReactNode }) {
  return <div className="panel">{children}</div>
}

export function PanelTitle({ children }: { children: React.ReactNode }) {
  return <div className="panelTitle">{children}</div>
}

export function PanelTop({ children }: { children: React.ReactNode }) {
  return <div className="panelTop">{children}</div>
}

export function PanelBody({ children }: { children: React.ReactNode }) {
  return <div className="panelBody">{children}</div>
}

