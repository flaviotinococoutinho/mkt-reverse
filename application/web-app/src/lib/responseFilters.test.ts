import { describe, expect, it } from 'vitest';
import { filterAndSortResponses } from './responseFilters';
import type { SupplierResponseView } from '../services/sourcingService';

const responses: SupplierResponseView[] = [
  {
    id: 'r-1',
    eventId: 'e-1',
    supplierId: 's-1',
    status: 'SUBMITTED',
    offerCents: 90000,
    currency: 'BRL',
    leadTimeDays: 5,
  },
  {
    id: 'r-2',
    eventId: 'e-1',
    supplierId: 's-2',
    status: 'SUBMITTED',
    offerCents: 85000,
    currency: 'BRL',
    leadTimeDays: 7,
  },
  {
    id: 'r-3',
    eventId: 'e-1',
    supplierId: 's-3',
    status: 'ACCEPTED',
    offerCents: 100000,
    currency: 'BRL',
    leadTimeDays: 2,
  },
  {
    id: '200',
    eventId: 'e-1',
    supplierId: 's-4',
    status: 'SUBMITTED',
    offerCents: 92000,
    currency: 'BRL',
    leadTimeDays: 4,
  },
  {
    id: '100',
    eventId: 'e-1',
    supplierId: 's-5',
    status: 'SUBMITTED',
    offerCents: 93000,
    currency: 'BRL',
    leadTimeDays: 3,
  },
];

describe('filterAndSortResponses', () => {
  it('filters by status and max offer', () => {
    const result = filterAndSortResponses(responses, {
      status: 'SUBMITTED',
      maxOfferCents: 87000,
      sortBy: 'BEST_PRICE',
    });

    expect(result).toHaveLength(1);
    expect(result[0].id).toBe('r-2');
  });

  it('sorts by fastest delivery', () => {
    const result = filterAndSortResponses(responses, {
      status: 'ALL',
      sortBy: 'FASTEST_DELIVERY',
    });

    expect(result.map((response) => response.id).slice(0, 3)).toEqual(['r-3', '100', '200']);
  });

  it('sorts by newest using numeric snowflake-like id when available', () => {
    const result = filterAndSortResponses(responses, {
      status: 'ALL',
      sortBy: 'NEWEST',
    });

    expect(result.slice(0, 2).map((response) => response.id)).toEqual(['200', '100']);
  });
});
