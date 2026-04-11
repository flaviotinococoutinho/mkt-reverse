import api from './api';

export interface SearchFilters {
  query?: string;
  mccCategoryCode?: number;
  visibility?: 'PUBLIC' | 'PRIVATE' | 'ALL';
  status?: 'PUBLISHED' | 'DRAFT' | 'ALL';
  sortBy?: 'PUBLISHED_AT' | 'TITLE' | 'RELEVANCE';
  sortDir?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
}

export interface OpportunitySearchResult {
  id: string;
  title: string;
  description?: string;
  productName?: string;
  status: string;
  mccCategoryCode?: number;
  visibility: string;
  publishedAt: string;
  relevance?: number;
}

export interface SearchResponse {
  items: OpportunitySearchResult[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

export interface CategoryFacet {
  mccCategoryCode: number;
  count: number;
  label: string;
}

export interface AutocompleteResult {
  id: string;
  title: string;
  productName?: string;
}

// Search API call
export async function searchOpportunities(filters: SearchFilters): Promise<SearchResponse> {
  const params = new URLSearchParams();
  
  if (filters.query) params.set('q', filters.query);
  if (filters.mccCategoryCode) params.set('mccCategoryCode', String(filters.mccCategoryCode));
  if (filters.visibility && filters.visibility !== 'ALL') params.set('visibility', filters.visibility);
  if (filters.status && filters.status !== 'ALL') params.set('status', filters.status);
  if (filters.sortBy) params.set('sortBy', filters.sortBy);
  if (filters.sortDir) params.set('sortDir', filters.sortDir);
  params.set('page', String(filters.page || 0));
  params.set('size', String(filters.size || 20));

  const response = await api.get<SearchResponse>(`/search/opportunities?${params.toString()}`);
  return response.data;
}

// Autocomplete for typeahead
export async function autocompleteOpportunities(prefix: string, limit = 10): Promise<AutocompleteResult[]> {
  const response = await api.get<AutocompleteResult[]>(`/search/autocomplete?q=${encodeURIComponent(prefix)}&limit=${limit}`);
  return response.data;
}

// Get category facets for filter sidebar
export async function getCategoryFacets(): Promise<CategoryFacet[]> {
  const response = await api.get<CategoryFacet[]>('/search/facets/categories');
  return response.data;
}

// MCC Category codes with labels
export const MCC_CATEGORIES = [
  { code: 174, label: 'Eletrônicos e Informática' },
  { code: 275, label: 'Vestuário e Acessórios' },
  { code: 553, label: 'Autos e Peças' },
  { code: 521, label: 'Móveis e Decoração' },
  { code: 571, label: 'Imóveis' },
  { code: 501, label: 'Médicos e Farmacêuticos' },
  { code: 581, label: 'Alimentos e Bebidas' },
  { code: 504, label: 'Máquinas e Equipamentos' },
  { code: 821, label: 'Serviços Profissionais' },
  { code: 829, label: 'Outros Serviços' },
];

// Get label for MCC code
export function getMccLabel(code: number): string {
  return MCC_CATEGORIES.find(c => c.code === code)?.label || 'Outros';
}

// Visibility options
export const VISIBILITY_OPTIONS = [
  { value: 'PUBLIC', label: 'Público' },
  { value: 'PRIVATE', label: 'Privado' },
];

// Status options
export const STATUS_OPTIONS = [
  { value: 'PUBLISHED', label: 'Publicado' },
  { value: 'IN_PROGRESS', label: 'Em Andamento' },
  { value: 'AWARDED', label: 'Concluído' },
  { value: 'CANCELLED', label: 'Cancelado' },
];

// Sort options
export const SORT_OPTIONS = [
  { value: 'PUBLISHED_AT_DESC', label: 'Mais recentes' },
  { value: 'PUBLISHED_AT_ASC', label: 'Mais antigos' },
  { value: 'TITLE_ASC', label: 'Título (A-Z)' },
  { value: 'TITLE_DESC', label: 'Título (Z-A)' },
  { value: 'RELEVANCE', label: 'Relevância' },
];

// Page size options
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];