import React from 'react'

type Props = {
  page: number
  totalPages?: number
  onPrev: () => void
  onNext: () => void
}

export function Pager({ page, totalPages, onPrev, onNext }: Props) {
  const canPrev = page > 0
  const canNext = totalPages ? page + 1 < totalPages : true

  return (
    <nav className="pager" aria-label="Paginação">
      <button
        className="btn"
        onClick={onPrev}
        disabled={!canPrev}
        aria-label="Página anterior"
        title="Página anterior"
      >
        ←
      </button>
      <div className="pagerText" aria-live="polite" aria-atomic="true">
        <span className="mono">page</span> {page + 1} <span className="muted">/ {totalPages || 1}</span>
      </div>
      <button
        className="btn"
        onClick={onNext}
        disabled={!canNext}
        aria-label="Próxima página"
        title="Próxima página"
      >
        →
      </button>
    </nav>
  )
}