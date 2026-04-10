export const OPPORTUNITY_VISIBILITY_OPTIONS = [
  { value: 'ALL', label: 'Todas (públicas + convite)' },
  { value: 'OPEN', label: 'Somente públicas' },
  { value: 'INVITE_ONLY', label: 'Somente por convite' },
] as const;

export const OPPORTUNITY_SORT_BY_OPTIONS = [
  { value: 'PUBLICATION_AT', label: 'Data de Publicação' },
  { value: 'DEADLINE', label: 'Prazo de Envio' },
  { value: 'TITLE', label: 'Título' },
] as const;

export const OPPORTUNITY_SORT_DIR_OPTIONS = [
  { value: 'DESC', label: 'Decrescente' },
  { value: 'ASC', label: 'Crescente' },
] as const;

export const OPPORTUNITY_PAGE_SIZE_OPTIONS = [10, 20, 50] as const;

export type OpportunityVisibility = (typeof OPPORTUNITY_VISIBILITY_OPTIONS)[number]['value'];
export type OpportunitySortBy = (typeof OPPORTUNITY_SORT_BY_OPTIONS)[number]['value'];
export type OpportunitySortDir = (typeof OPPORTUNITY_SORT_DIR_OPTIONS)[number]['value'];
export type OpportunityPageSize = (typeof OPPORTUNITY_PAGE_SIZE_OPTIONS)[number];

export interface OpportunityQueryState {
  q: string;
  mccCategoryCode: string;
  visibility: OpportunityVisibility;
  sortBy: OpportunitySortBy;
  sortDir: OpportunitySortDir;
  page: number;
  size: OpportunityPageSize;
}

const VISIBILITY_LABELS: Record<OpportunityVisibility, string> = {
  ALL: 'Todas',
  OPEN: 'Pública',
  INVITE_ONLY: 'Convite',
};

export function getOpportunityVisibilityLabel(visibility: OpportunityVisibility): string {
  return VISIBILITY_LABELS[visibility] ?? visibility;
}

export function parseOpportunityQueryParams(
  searchParams: URLSearchParams,
): OpportunityQueryState {
  const initialVisibility = searchParams.get('visibility');
  const initialSortBy = searchParams.get('sortBy');
  const initialSortDir = searchParams.get('sortDir');
  const initialPage = Number.parseInt(searchParams.get('page') ?? '0', 10);
  const initialSize = Number.parseInt(
    searchParams.get('size') ?? String(OPPORTUNITY_PAGE_SIZE_OPTIONS[0]),
    10,
  );

  const isValidVisibility = OPPORTUNITY_VISIBILITY_OPTIONS.some(
    (option) => option.value === initialVisibility,
  );
  const isValidSortBy = OPPORTUNITY_SORT_BY_OPTIONS.some(
    (option) => option.value === initialSortBy,
  );
  const isValidSortDir = OPPORTUNITY_SORT_DIR_OPTIONS.some(
    (option) => option.value === initialSortDir,
  );
  const isValidPageSize = OPPORTUNITY_PAGE_SIZE_OPTIONS.some(
    (option) => option === initialSize,
  );

  return {
    q: searchParams.get('q')?.trim() ?? '',
    mccCategoryCode: searchParams.get('mcc')?.trim() ?? '',
    visibility: (isValidVisibility
      ? initialVisibility
      : 'ALL') as OpportunityVisibility,
    sortBy: (isValidSortBy
      ? initialSortBy
      : 'PUBLICATION_AT') as OpportunitySortBy,
    sortDir: (isValidSortDir
      ? initialSortDir
      : 'DESC') as OpportunitySortDir,
    page: Number.isNaN(initialPage) ? 0 : Math.max(0, initialPage),
    size: isValidPageSize
      ? (initialSize as OpportunityPageSize)
      : OPPORTUNITY_PAGE_SIZE_OPTIONS[0],
  };
}

export function toOpportunityQueryParams(state: OpportunityQueryState): URLSearchParams {
  const nextParams = new URLSearchParams();

  if (state.q) {
    nextParams.set('q', state.q);
  }
  if (state.mccCategoryCode) {
    nextParams.set('mcc', state.mccCategoryCode);
  }
  if (state.visibility !== 'ALL') {
    nextParams.set('visibility', state.visibility);
  }
  if (state.sortBy !== 'PUBLICATION_AT') {
    nextParams.set('sortBy', state.sortBy);
  }
  if (state.sortDir !== 'DESC') {
    nextParams.set('sortDir', state.sortDir);
  }
  if (state.page > 0) {
    nextParams.set('page', String(state.page));
  }
  if (state.size !== OPPORTUNITY_PAGE_SIZE_OPTIONS[0]) {
    nextParams.set('size', String(state.size));
  }

  return nextParams;
}
