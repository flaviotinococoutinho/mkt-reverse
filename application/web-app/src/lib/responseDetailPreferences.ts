export const RESPONSE_STATUS_OPTIONS = ['ALL', 'SUBMITTED', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'] as const;
export type ResponseStatusFilter = (typeof RESPONSE_STATUS_OPTIONS)[number];

export const RESPONSE_SORT_OPTIONS = ['BEST_PRICE', 'FASTEST_DELIVERY', 'NEWEST'] as const;
export type ResponseSortMode = (typeof RESPONSE_SORT_OPTIONS)[number];

export interface ResponseDetailPreferences {
  status: ResponseStatusFilter;
  sortBy: ResponseSortMode;
  maxOfferInput: string;
  favoriteResponseIds: string[];
  comparisonIds: string[];
  showOnlyFavorites: boolean;
}

const DEFAULTS: ResponseDetailPreferences = {
  status: 'ALL',
  sortBy: 'BEST_PRICE',
  maxOfferInput: '',
  favoriteResponseIds: [],
  comparisonIds: [],
  showOnlyFavorites: false,
};

const splitIds = (value: string | null): string[] => {
  if (!value) {
    return [];
  }

  return Array.from(new Set(value
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item.length > 0)));
};

export function parseResponseDetailPreferences(searchParams: URLSearchParams): ResponseDetailPreferences {
  const status = searchParams.get('status');
  const sortBy = searchParams.get('sortBy');
  const maxOfferRaw = searchParams.get('maxOffer')?.trim() ?? '';

  const maxOfferInput = /^\d+(?:[.,]\d{0,2})?$/.test(maxOfferRaw) ? maxOfferRaw : '';

  return {
    status: RESPONSE_STATUS_OPTIONS.includes(status as ResponseStatusFilter)
      ? (status as ResponseStatusFilter)
      : DEFAULTS.status,
    sortBy: RESPONSE_SORT_OPTIONS.includes(sortBy as ResponseSortMode)
      ? (sortBy as ResponseSortMode)
      : DEFAULTS.sortBy,
    maxOfferInput,
    favoriteResponseIds: splitIds(searchParams.get('fav')),
    comparisonIds: splitIds(searchParams.get('cmp')).slice(0, 2),
    showOnlyFavorites: searchParams.get('onlyFav') === '1',
  };
}

export function toResponseDetailPreferencesQueryParams(preferences: ResponseDetailPreferences): URLSearchParams {
  const params = new URLSearchParams();

  if (preferences.status !== DEFAULTS.status) {
    params.set('status', preferences.status);
  }

  if (preferences.sortBy !== DEFAULTS.sortBy) {
    params.set('sortBy', preferences.sortBy);
  }

  const normalizedMaxOffer = preferences.maxOfferInput.trim();
  if (normalizedMaxOffer.length > 0) {
    params.set('maxOffer', normalizedMaxOffer);
  }

  if (preferences.favoriteResponseIds.length > 0) {
    params.set('fav', Array.from(new Set(preferences.favoriteResponseIds)).join(','));
  }

  if (preferences.comparisonIds.length > 0) {
    params.set('cmp', Array.from(new Set(preferences.comparisonIds)).slice(0, 2).join(','));
  }

  if (preferences.showOnlyFavorites) {
    params.set('onlyFav', '1');
  }

  return params;
}
