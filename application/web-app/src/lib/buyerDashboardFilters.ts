export const BUYER_STATUS_FILTERS = [
  'ALL',
  'DRAFT',
  'PUBLISHED',
  'IN_PROGRESS',
  'AWARDED',
  'CANCELLED',
] as const;

export type BuyerStatusFilter = (typeof BUYER_STATUS_FILTERS)[number];

export interface BuyerDashboardFilters {
  searchQuery: string;
  status: BuyerStatusFilter;
  page: number;
}

const DEFAULT_FILTERS: BuyerDashboardFilters = {
  searchQuery: '',
  status: 'ALL',
  page: 0,
};

const isValidStatus = (value: string | null): value is BuyerStatusFilter => {
  if (!value) {
    return false;
  }

  return BUYER_STATUS_FILTERS.includes(value as BuyerStatusFilter);
};

export function parseBuyerDashboardFilters(searchParams: URLSearchParams): BuyerDashboardFilters {
  const rawQuery = searchParams.get('q')?.trim() ?? '';
  const rawStatus = searchParams.get('status');
  const rawPage = Number.parseInt(searchParams.get('page') ?? '0', 10);

  return {
    searchQuery: rawQuery,
    status: isValidStatus(rawStatus) ? rawStatus : DEFAULT_FILTERS.status,
    page: Number.isNaN(rawPage) ? DEFAULT_FILTERS.page : Math.max(0, rawPage),
  };
}

export function toBuyerDashboardQueryParams(filters: BuyerDashboardFilters): URLSearchParams {
  const params = new URLSearchParams();

  const normalizedQuery = filters.searchQuery.trim();
  if (normalizedQuery) {
    params.set('q', normalizedQuery);
  }

  if (filters.status !== DEFAULT_FILTERS.status) {
    params.set('status', filters.status);
  }

  if (filters.page > 0) {
    params.set('page', String(filters.page));
  }

  return params;
}
