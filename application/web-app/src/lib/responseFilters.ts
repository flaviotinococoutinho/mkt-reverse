import type { SupplierResponseView } from '../services/sourcingService';

export type ResponseStatusFilter = 'ALL' | 'SUBMITTED' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN';
export type ResponseSortMode = 'BEST_PRICE' | 'FASTEST_DELIVERY' | 'NEWEST';

export interface ResponseFilterOptions {
  status: ResponseStatusFilter;
  maxOfferCents?: number;
  sortBy: ResponseSortMode;
}

export function filterAndSortResponses(
  responses: SupplierResponseView[],
  options: ResponseFilterOptions,
): SupplierResponseView[] {
  const filtered = responses.filter((response) => {
    if (options.status !== 'ALL' && response.status !== options.status) {
      return false;
    }

    if (typeof options.maxOfferCents === 'number' && response.offerCents > options.maxOfferCents) {
      return false;
    }

    return true;
  });

  const sorted = [...filtered];

  if (options.sortBy === 'BEST_PRICE') {
    sorted.sort((a, b) => a.offerCents - b.offerCents);
    return sorted;
  }

  if (options.sortBy === 'FASTEST_DELIVERY') {
    sorted.sort((a, b) => {
      const aLead = a.leadTimeDays ?? Number.MAX_SAFE_INTEGER;
      const bLead = b.leadTimeDays ?? Number.MAX_SAFE_INTEGER;
      return aLead - bLead;
    });
    return sorted;
  }

  if (options.sortBy === 'NEWEST') {
    sorted.sort((a, b) => {
      const aId = Number(a.id);
      const bId = Number(b.id);
      const aIsNumeric = Number.isFinite(aId);
      const bIsNumeric = Number.isFinite(bId);

      if (aIsNumeric && bIsNumeric) {
        return bId - aId;
      }

      if (aIsNumeric) {
        return -1;
      }

      if (bIsNumeric) {
        return 1;
      }

      return b.id.localeCompare(a.id);
    });
    return sorted;
  }

  return sorted;
}
