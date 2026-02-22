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

export type OpportunityVisibility = (typeof OPPORTUNITY_VISIBILITY_OPTIONS)[number]['value'];
export type OpportunitySortBy = (typeof OPPORTUNITY_SORT_BY_OPTIONS)[number]['value'];
export type OpportunitySortDir = (typeof OPPORTUNITY_SORT_DIR_OPTIONS)[number]['value'];

const VISIBILITY_LABELS: Record<OpportunityVisibility, string> = {
  ALL: 'Todas',
  OPEN: 'Pública',
  INVITE_ONLY: 'Convite',
};

export function getOpportunityVisibilityLabel(visibility: OpportunityVisibility): string {
  return VISIBILITY_LABELS[visibility] ?? visibility;
}
