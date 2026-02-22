import { describe, expect, it } from 'vitest';
import {
  parseBuyerDashboardFilters,
  toBuyerDashboardQueryParams,
  type BuyerDashboardFilters,
} from './buyerDashboardFilters';

describe('buyerDashboardFilters', () => {
  it('parseia defaults quando query params estão vazios ou inválidos', () => {
    const parsed = parseBuyerDashboardFilters(
      new URLSearchParams('status=INVALID&page=-9&q=   '),
    );

    expect(parsed).toEqual({
      searchQuery: '',
      status: 'ALL',
      page: 0,
    });
  });

  it('parseia filtros válidos preservando status e paginação', () => {
    const parsed = parseBuyerDashboardFilters(
      new URLSearchParams('q= notebook gamer &status=IN_PROGRESS&page=3'),
    );

    expect(parsed).toEqual({
      searchQuery: 'notebook gamer',
      status: 'IN_PROGRESS',
      page: 3,
    });
  });

  it('serializa somente valores não default', () => {
    const defaults: BuyerDashboardFilters = {
      searchQuery: '',
      status: 'ALL',
      page: 0,
    };

    expect(toBuyerDashboardQueryParams(defaults).toString()).toBe('');
  });

  it('serializa filtros ativos de forma determinística', () => {
    const filters: BuyerDashboardFilters = {
      searchQuery: ' tv 55 ',
      status: 'PUBLISHED',
      page: 2,
    };

    expect(toBuyerDashboardQueryParams(filters).toString()).toBe(
      'q=tv+55&status=PUBLISHED&page=2',
    );
  });
});
