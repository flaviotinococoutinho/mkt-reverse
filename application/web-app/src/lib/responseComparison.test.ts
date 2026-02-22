import { describe, expect, it } from 'vitest';
import { toggleComparisonSelection } from './responseComparison';

describe('toggleComparisonSelection', () => {
  it('adds a response when below the limit', () => {
    const result = toggleComparisonSelection(['r-1'], 'r-2');

    expect(result).toEqual({
      nextIds: ['r-1', 'r-2'],
      limitReached: false,
    });
  });

  it('removes a response when toggled again', () => {
    const result = toggleComparisonSelection(['r-1', 'r-2'], 'r-1');

    expect(result).toEqual({
      nextIds: ['r-2'],
      limitReached: false,
    });
  });

  it('keeps selection unchanged and flags when at max limit', () => {
    const result = toggleComparisonSelection(['r-1', 'r-2'], 'r-3');

    expect(result).toEqual({
      nextIds: ['r-1', 'r-2'],
      limitReached: true,
    });
  });
});

