// @vitest-environment jsdom

import React from 'react'
import { describe, expect, it, vi, afterEach } from 'vitest'
import { render, screen, fireEvent, cleanup } from '@testing-library/react'

import { Pager } from './Pager'

afterEach(() => {
  cleanup()
})

describe('Pager', () => {
  it('renders with accessibility attributes', () => {
    render(<Pager page={1} totalPages={5} onPrev={() => {}} onNext={() => {}} />)

    // Check for navigation role
    expect(screen.getByRole('navigation', { name: /pagination/i })).toBeTruthy()

    // Check for buttons with accessible labels
    expect(screen.getByRole('button', { name: /previous page/i })).toBeTruthy()
    expect(screen.getByRole('button', { name: /next page/i })).toBeTruthy()
  })

  it('handles click events', () => {
    const onPrev = vi.fn()
    const onNext = vi.fn()
    render(<Pager page={1} totalPages={5} onPrev={onPrev} onNext={onNext} />)

    fireEvent.click(screen.getByRole('button', { name: /previous page/i }))
    expect(onPrev).toHaveBeenCalled()

    fireEvent.click(screen.getByRole('button', { name: /next page/i }))
    expect(onNext).toHaveBeenCalled()
  })

  it('disables previous button on first page', () => {
    render(<Pager page={0} totalPages={5} onPrev={() => {}} onNext={() => {}} />)
    const prevBtn = screen.getByRole('button', { name: /previous page/i }) as HTMLButtonElement
    const nextBtn = screen.getByRole('button', { name: /next page/i }) as HTMLButtonElement

    expect(prevBtn.disabled).toBe(true)
    expect(nextBtn.disabled).toBe(false)
  })

  it('disables next button on last page', () => {
    render(<Pager page={4} totalPages={5} onPrev={() => {}} onNext={() => {}} />)
    const prevBtn = screen.getByRole('button', { name: /previous page/i }) as HTMLButtonElement
    const nextBtn = screen.getByRole('button', { name: /next page/i }) as HTMLButtonElement

    expect(prevBtn.disabled).toBe(false)
    expect(nextBtn.disabled).toBe(true)
  })
})
