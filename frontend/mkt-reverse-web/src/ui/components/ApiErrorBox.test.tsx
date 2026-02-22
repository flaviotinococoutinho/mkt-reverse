// @vitest-environment jsdom

import React from 'react'
import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'

import { ApiErrorBox } from './ApiErrorBox'

describe('ApiErrorBox', () => {
  it('renders message + correlation id when present', () => {
    render(
      <ApiErrorBox
        error={{
          message: 'boom',
          correlationId: 'corr-123',
          problem: { code: 'X', correlationId: 'corr-999' },
        }}
      />,
    )

    expect(screen.getByRole('alert')).toBeTruthy()
    expect(screen.getByText(/boom/)).toBeTruthy()
    expect(screen.getByText(/code/)).toBeTruthy()
    expect(screen.getByText(/X/)).toBeTruthy()
    // prefer problem.correlationId
    expect(screen.getByText(/corr-999/)).toBeTruthy()
  })
})
