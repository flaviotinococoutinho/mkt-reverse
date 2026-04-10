import { describe, expect, it } from 'vitest';
import { getPostAcceptanceChecklist } from './negotiationChecklist';

describe('getPostAcceptanceChecklist', () => {
  it('returns base checklist when event is still open', () => {
    const checklist = getPostAcceptanceChecklist('IN_PROGRESS');

    expect(checklist).toHaveLength(3);
    expect(checklist.map((item) => item.key)).toEqual(['contact', 'timeline', 'evidence']);
  });

  it('adds closure guidance when event is awarded/closed', () => {
    const checklist = getPostAcceptanceChecklist('AWARDED');

    expect(checklist).toHaveLength(4);
    expect(checklist.at(-1)?.key).toBe('closure');
  });
});

