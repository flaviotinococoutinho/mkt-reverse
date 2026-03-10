import { expect, test, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Pager } from './Pager';

test('Pager renders with accessibility attributes', () => {
  const onPrev = vi.fn();
  const onNext = vi.fn();

  render(
    <Pager page={1} totalPages={5} onPrev={onPrev} onNext={onNext} />
  );

  const prevButton = screen.getByRole('button', { name: 'Previous page' });
  const nextButton = screen.getByRole('button', { name: 'Next page' });

  expect(prevButton).toBeDefined();
  expect(nextButton).toBeDefined();

  expect(prevButton.getAttribute('title')).toBe('Previous page');
  expect(nextButton.getAttribute('title')).toBe('Next page');
});