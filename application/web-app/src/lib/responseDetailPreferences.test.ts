import { describe, expect, it } from 'vitest';
import {
  parseResponseDetailPreferences,
  toResponseDetailPreferencesQueryParams,
  type ResponseDetailPreferences,
} from './responseDetailPreferences';

describe('responseDetailPreferences', () => {
  it('parses defaults when query params are empty or invalid', () => {
    const parsed = parseResponseDetailPreferences(
      new URLSearchParams('status=INVALID&sortBy=ANY&maxOffer=abc&onlyFav=0'),
    );

    expect(parsed).toEqual({
      status: 'ALL',
      sortBy: 'BEST_PRICE',
      maxOfferInput: '',
      favoriteResponseIds: [],
      comparisonIds: [],
      showOnlyFavorites: false,
    });
  });

  it('parses valid status, sort, prices and ids', () => {
    const parsed = parseResponseDetailPreferences(
      new URLSearchParams('status=SUBMITTED&sortBy=NEWEST&maxOffer=1200,50&fav=1,2,1&cmp=a,b,c&onlyFav=1'),
    );

    expect(parsed).toEqual({
      status: 'SUBMITTED',
      sortBy: 'NEWEST',
      maxOfferInput: '1200,50',
      favoriteResponseIds: ['1', '2'],
      comparisonIds: ['a', 'b'],
      showOnlyFavorites: true,
    });
  });

  it('serializes only non-default values', () => {
    const defaults: ResponseDetailPreferences = {
      status: 'ALL',
      sortBy: 'BEST_PRICE',
      maxOfferInput: '',
      favoriteResponseIds: [],
      comparisonIds: [],
      showOnlyFavorites: false,
    };

    expect(toResponseDetailPreferencesQueryParams(defaults).toString()).toBe('');
  });

  it('serializes active preferences in deterministic shape', () => {
    const preferences: ResponseDetailPreferences = {
      status: 'ACCEPTED',
      sortBy: 'FASTEST_DELIVERY',
      maxOfferInput: '999.99',
      favoriteResponseIds: ['r1', 'r2', 'r1'],
      comparisonIds: ['r3', 'r4', 'r5'],
      showOnlyFavorites: true,
    };

    const params = toResponseDetailPreferencesQueryParams(preferences);

    expect(params.toString()).toBe(
      'status=ACCEPTED&sortBy=FASTEST_DELIVERY&maxOffer=999.99&fav=r1%2Cr2&cmp=r3%2Cr4&onlyFav=1',
    );
  });
});
