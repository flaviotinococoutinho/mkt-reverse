import { render, screen } from '@testing-library/react'
import { Pager } from './Pager'
import { describe, it, expect, vi } from 'vitest'

describe('Pager UX/A11y', () => {
  it('has correct ARIA labels for accessibility', () => {
    render(<Pager page={1} totalPages={5} onPrev={vi.fn()} onNext={vi.fn()} />)

    const nav = screen.getByRole('navigation')
    expect(nav.getAttribute('aria-label')).toBe('Pagination')

    const prevBtn = screen.getByLabelText('Previous page')
    expect(prevBtn).toBeDefined()

    const nextBtn = screen.getByLabelText('Next page')
    expect(nextBtn).toBeDefined()

    const current = screen.getByText('page')
    expect(current.parentElement?.getAttribute('aria-current')).toBe('page')
  })
})
