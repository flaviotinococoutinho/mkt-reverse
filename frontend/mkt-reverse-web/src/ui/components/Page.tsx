import React from 'react'

export function Page({ children }: { children: React.ReactNode }) {
  return <section className="page">{children}</section>
}

export function Hero({ children }: { children: React.ReactNode }) {
  return <div className="hero">{children}</div>
}

export function HeroLeft({ children }: { children: React.ReactNode }) {
  return <div className="heroLeft">{children}</div>
}

export function HeroRight({ children }: { children: React.ReactNode }) {
  return <div className="heroRight">{children}</div>
}

