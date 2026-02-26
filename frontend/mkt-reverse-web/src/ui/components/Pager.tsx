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
    <div className="pager" role="navigation" aria-label="Pagination">
      <button
        className="btn"
        onClick={onPrev}
        disabled={!canPrev}
        aria-label="Previous Page"
        title="Previous Page"
      >
        <span aria-hidden="true">←</span>
      </button>
      <div className="pagerText">
        <span className="mono">page</span> {page + 1} <span className="muted">/ {totalPages || 1}</span>
      </div>
      <button
        className="btn"
        onClick={onNext}
        disabled={!canNext}
        aria-label="Next Page"
        title="Next Page"
      >
        <span aria-hidden="true">→</span>
      </button>
    </div>
  )
}
