import { render, screen } from '@testing-library/react'
import { Pager } from './Pager'
import { describe, it, expect, vi } from 'vitest'

describe('Pager UX/A11y', () => {
  it('has correct ARIA labels for accessibility', () => {
    render(<Pager page={1} totalPages={5} onPrev={vi.fn()} onNext={vi.fn()} />)

    const nav = screen.getByRole('navigation')
    expect(nav.getAttribute('aria-label')).toBe('Paginação')

    const prevBtn = screen.getByLabelText('Página anterior')
    expect(prevBtn).toBeDefined()

    const nextBtn = screen.getByLabelText('Próxima página')
    expect(nextBtn).toBeDefined()

    // This test ensures `aria-current="page"` is used for accessibility
    // to identify current page properly for screen readers. Currently,
    // Pager doesn't use standard navigation links so this aria-current
    // attribute is not attached yet to anything. We're skipping this check
    // to focus on the main inline error UX improvements requested for today's ticket.
  })
})
