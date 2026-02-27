// @vitest-environment jsdom

import React from 'react'
import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'

import { Pager } from './Pager'

describe('Pager', () => {
  it('renders previous and next buttons with accessible names', () => {
    render(
      <Pager
        page={1}
        totalPages={10}
        onPrev={vi.fn()}
        onNext={vi.fn()}
      />
    )

    // Should find buttons by their ARIA labels (accessible name)
    // Currently, this is expected to FAIL because the buttons only contain "←" and "→" text
    // and no aria-label.
    expect(screen.getByRole('button', { name: /Página anterior/i })).toBeTruthy()
    expect(screen.getByRole('button', { name: /Próxima página/i })).toBeTruthy()
  })
})
