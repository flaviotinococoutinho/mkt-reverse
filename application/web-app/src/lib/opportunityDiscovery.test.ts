import { describe, expect, it } from 'vitest';
import {
  getOpportunityVisibilityLabel,
  parseOpportunityQueryParams,
  toOpportunityQueryParams,
} from './opportunityDiscovery';

describe('opportunityDiscovery', () => {
  describe('getOpportunityVisibilityLabel', () => {
    it('returns localized labels for known values', () => {
      expect(getOpportunityVisibilityLabel('ALL')).toBe('Todas');
      expect(getOpportunityVisibilityLabel('OPEN')).toBe('Pública');
      expect(getOpportunityVisibilityLabel('INVITE_ONLY')).toBe('Convite');
    });
  });

  describe('parseOpportunityQueryParams', () => {
    it('uses defaults when params are missing or invalid', () => {
      const parsed = parseOpportunityQueryParams(new URLSearchParams('sortBy=INVALID&size=999&page=-2'));

      expect(parsed).toEqual({
        q: '',
        mccCategoryCode: '',
        visibility: 'ALL',
        sortBy: 'PUBLICATION_AT',
        sortDir: 'DESC',
        page: 0,
        size: 10,
      });
    });

    it('keeps valid values from querystring', () => {
      const parsed = parseOpportunityQueryParams(
        new URLSearchParams('q=freio&mcc=5533&visibility=OPEN&sortBy=DEADLINE&sortDir=ASC&page=2&size=20'),
      );

      expect(parsed).toEqual({
        q: 'freio',
        mccCategoryCode: '5533',
        visibility: 'OPEN',
        sortBy: 'DEADLINE',
        sortDir: 'ASC',
        page: 2,
        size: 20,
      });
    });
  });

  describe('toOpportunityQueryParams', () => {
    it('omits default values to keep URL clean', () => {
      const params = toOpportunityQueryParams({
        q: '',
        mccCategoryCode: '',
        visibility: 'ALL',
        sortBy: 'PUBLICATION_AT',
        sortDir: 'DESC',
        page: 0,
        size: 10,
      });

      expect(params.toString()).toBe('');
    });

    it('serializes non-default values', () => {
      const params = toOpportunityQueryParams({
        q: 'pastilha',
        mccCategoryCode: '5533',
        visibility: 'INVITE_ONLY',
        sortBy: 'TITLE',
        sortDir: 'ASC',
        page: 1,
        size: 50,
      });

      expect(params.toString()).toBe(
        'q=pastilha&mcc=5533&visibility=INVITE_ONLY&sortBy=TITLE&sortDir=ASC&page=1&size=50',
      );
    });
  });
});
