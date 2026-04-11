import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { AppHeader } from '../../components/layout/AppHeader';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { useAuth } from '../../context/useAuth';
import { useToast } from '../../components/ui/feedback';
import { Loading, ListSkeleton, NoResults } from '../../components/ui/feedback';
import {
  searchOpportunities,
  autocompleteOpportunities,
  getCategoryFacets,
  getMccLabel,
  MCC_CATEGORIES,
  VISIBILITY_OPTIONS,
  SORT_OPTIONS,
  PAGE_SIZE_OPTIONS,
  type SearchFilters,
  type SearchResponse,
  type CategoryFacet,
} from '../../services/searchService';
import {
  Search,
  Filter,
  X,
  ChevronDown,
  ChevronUp,
  Loader2,
} from 'lucide-react';

// Debounce hook for autocomplete
function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(handler);
  }, [value, delay]);

  return debouncedValue;
}

export default function OpportunitiesSearchPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { user } = useAuth();
  const { error: showError, success } = useToast();

  const [results, setResults] = useState<SearchResponse | null>(null);
  const [facets, setFacets] = useState<CategoryFacet[]>([]);
  const [loading, setLoading] = useState(true);
  const [showFilters, setShowFilters] = useState(false);

  // Search state
  const [query, setQuery] = useState(searchParams.get('q') || '');
  const [appliedQuery, setAppliedQuery] = useState(searchParams.get('q') || '');
  const [suggestions, setSuggestions] = useState<{ id: string; title: string }[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);

  // Filter state
  const [selectedCategory, setSelectedCategory] = useState<number | null>(
    searchParams.get('category') ? Number(searchParams.get('category')) : null
  );
  const [selectedVisibility, setSelectedVisibility] = useState<string>(
    searchParams.get('visibility') || 'PUBLIC'
  );
  const [sortBy, setSortBy] = useState<string>(
    searchParams.get('sortBy') || 'PUBLISHED_AT_DESC'
  );
  const [page, setPage] = useState<number>(
    searchParams.get('page') ? Number(searchParams.get('page')) : 0
  );
  const [pageSize, setPageSize] = useState<number>(
    searchParams.get('size') ? Number(searchParams.get('size')) : 20
  );

  const debouncedQuery = useDebounce(query, 300);

  // Load autocomplete suggestions
  useEffect(() => {
    if (debouncedQuery.length < 2) {
      setSuggestions([]);
      return;
    }

    autocompleteOpportunities(debouncedQuery, 5)
      .then(setSuggestions)
      .catch(() => setSuggestions([]));
  }, [debouncedQuery]);

  // Load facets
  useEffect(() => {
    getCategoryFacets()
      .then(setFacets)
      .catch(() => setFacets([]));
  }, []);

  // Search function
  const performSearch = useCallback(async () => {
    setLoading(true);

    const filters: SearchFilters = {
      query: appliedQuery || undefined,
      mccCategoryCode: selectedCategory || undefined,
      visibility: selectedVisibility as 'PUBLIC' | 'PRIVATE',
      sortBy: sortBy.includes('RELEVANCE') ? 'RELEVANCE' : 'PUBLISHED_AT',
      sortDir: sortBy.includes('ASC') ? 'ASC' : 'DESC',
      page,
      size: pageSize,
    };

    try {
      const response = await searchOpportunities(filters);
      setResults(response);
      
      // Update URL
      const params = new URLSearchParams();
      if (appliedQuery) params.set('q', appliedQuery);
      if (selectedCategory) params.set('category', String(selectedCategory));
      if (selectedVisibility !== 'PUBLIC') params.set('visibility', selectedVisibility);
      if (sortBy !== 'PUBLISHED_AT_DESC') params.set('sortBy', sortBy);
      if (page > 0) params.set('page', String(page));
      if (pageSize !== 20) params.set('size', String(pageSize));
      setSearchParams(params, { replace: true });
    } catch (err) {
      console.error('Search failed:', err);
      showError('Erro na busca', 'Não foi possível buscar oportunidades.');
    } finally {
      setLoading(false);
    }
  }, [appliedQuery, selectedCategory, selectedVisibility, sortBy, page, pageSize, showError, setSearchParams]);

  // Initial load + filter changes
  useEffect(() => {
    performSearch();
  }, [performSearch]);

  const clearFilters = () => {
    setQuery('');
    setAppliedQuery('');
    setSelectedCategory(null);
    setSelectedVisibility('PUBLIC');
    setSortBy('PUBLISHED_AT_DESC');
    setPage(0);
    setPageSize(20);
  };

  const totalPages = results ? Math.ceil(results.total / pageSize) : 0;

  return (
    <div className="min-h-screen bg-ink text-zinc-200 font-sans">
      <AppHeader />

      <main className="container mx-auto px-4 py-8">
        {/* Page Title */}
        <div className="mb-8">
          <h2 className="text-3xl font-serif text-zinc-100 mb-2">
            Buscar Oportunidades
          </h2>
          <p className="text-zinc-400">
            Encontre solicitações que matches com seu negócio
          </p>
        </div>

        {/* Search Bar */}
        <div className="auction-panel p-4 mb-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-zinc-500" />
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  setAppliedQuery(query);
                }
              }}
              placeholder="Buscar por título, produto, descrição..."
              className="w-full pl-10 pr-20 py-3 bg-ink border border-stroke rounded-lg text-zinc-200 placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-citrus"
            />
            {query && (
              <button
                onClick={() => { setQuery(''); setAppliedQuery(''); }}
                className="absolute right-3 top-1/2 -translate-y-1/2"
              >
                <X className="w-5 h-5 text-zinc-500 hover:text-zinc-300" />
              </button>
            )}

            {/* Autocomplete Suggestions */}
            {showSuggestions && suggestions.length > 0 && (
              <div className="absolute top-full left-0 right-0 mt-1 bg-zinc-800 border border-stroke rounded-lg shadow-lg z-10">
                {suggestions.map((s) => (
                  <button
                    key={s.id}
                    onClick={() => {
                      setQuery(s.title);
                      setAppliedQuery(s.title);
                      setShowSuggestions(false);
                    }}
                    className="w-full px-4 py-2 text-left text-zinc-300 hover:bg-zinc-700"
                  >
                    {s.title}
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="flex gap-2 mt-4">
            <Button
              variant="secondary"
              onClick={() => setShowFilters(!showFilters)}
              className="flex items-center gap-2"
            >
              <Filter className="w-4 h-4" />
              Filtros
              {showFilters ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </Button>
            <Button variant="outline" onClick={clearFilters}>
              Limpar
            </Button>
          </div>

          {/* Expanded Filters */}
          {showFilters && (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4 pt-4 border-t border-stroke">
              {/* Category Filter */}
              <div>
                <label className="block text-sm font-medium text-zinc-400 mb-2">
                  Categoria
                </label>
                <select
                  value={selectedCategory || ''}
                  onChange={(e) => setSelectedCategory(e.target.value ? Number(e.target.value) : null)}
                  className="w-full px-3 py-2 bg-ink border border-stroke rounded-md text-zinc-200"
                >
                  <option value="">Todas as categorias</option>
                  {MCC_CATEGORIES.map((cat) => (
                    <option key={cat.code} value={cat.code}>{cat.label}</option>
                  ))}
                </select>
              </div>

              {/* Visibility Filter */}
              <div>
                <label className="block text-sm font-medium text-zinc-400 mb-2">
                  Visibilidade
                </label>
                <select
                  value={selectedVisibility}
                  onChange={(e) => setSelectedVisibility(e.target.value)}
                  className="w-full px-3 py-2 bg-ink border border-stroke rounded-md text-zinc-200"
                >
                  <option value="ALL">Todas</option>
                  {VISIBILITY_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>

              {/* Sort */}
              <div>
                <label className="block text-sm font-medium text-zinc-400 mb-2">
                  Ordenar por
                </label>
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="w-full px-3 py-2 bg-ink border border-stroke rounded-md text-zinc-200"
                >
                  {SORT_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>
            </div>
          )}
        </div>

        {/* Active Filters */}
        {(selectedCategory || selectedVisibility !== 'PUBLIC' || appliedQuery) && (
          <div className="flex flex-wrap gap-2 mb-4">
            {appliedQuery && (
              <span className="px-3 py-1 bg-citrus/20 text-citrus text-sm rounded-full">
                Busca: "{appliedQuery}"
              </span>
            )}
            {selectedCategory && (
              <span className="px-3 py-1 bg-citrus/20 text-citrus text-sm rounded-full flex items-center gap-2">
                {getMccLabel(selectedCategory)}
                <button onClick={() => setSelectedCategory(null)}>
                  <X className="w-3 h-3" />
                </button>
              </span>
            )}
            {selectedVisibility !== 'PUBLIC' && (
              <span className="px-3 py-1 bg-citrus/20 text-citrus text-sm rounded-full flex items-center gap-2">
                {selectedVisibility}
                <button onClick={() => setSelectedVisibility('PUBLIC')}>
                  <X className="w-3 h-3" />
                </button>
              </span>
            )}
          </div>
        )}

        {/* Category Facets (if no query) */}
        {!appliedQuery && facets.length > 0 && (
          <div className="mb-6">
            <h3 className="text-sm font-medium text-zinc-400 mb-2">Categorias populares</h3>
            <div className="flex flex-wrap gap-2">
              {facets.slice(0, 6).map((facet) => (
                <button
                  key={facet.mccCategoryCode}
                  onClick={() => setSelectedCategory(facet.mccCategoryCode)}
                  className={`px-3 py-1 rounded-full text-sm transition-colors ${
                    selectedCategory === facet.mccCategoryCode
                      ? 'bg-citrus text-ink'
                      : 'bg-zinc-800 text-zinc-300 hover:bg-zinc-700'
                  }`}
                >
                  {facet.label} ({facet.count})
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Results */}
        {loading ? (
          <div className="auction-panel p-8">
            <ListSkeleton count={5} />
          </div>
        ) : results?.total === 0 ? (
          <NoResults action={{ label: 'Limpar filtros', onClick: clearFilters }} />
        ) : (
          <>
            {/* Results count */}
            <p className="text-sm text-zinc-400 mb-4">
              {results?.total} oportunidade{results?.total !== 1 ? 's' : ''} encontrada{results?.total !== 1 ? 's' : ''}
              {appliedQuery && ` para "${appliedQuery}"`}
            </p>

            {/* Results list */}
            <div className="space-y-4">
              {results?.items.map((item) => (
                <div
                  key={item.id}
                  className="auction-panel p-6 hover:border-stroke/50 transition-colors cursor-pointer"
                  onClick={() => navigate(`/supplier/opportunities/${item.id}`)}
                >
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <h3 className="text-lg font-medium text-zinc-100 mb-1">
                        {item.title}
                      </h3>
                      <p className="text-sm text-zinc-400 mb-2">
                        {item.productName || item.description || 'Sem descrição'}
                      </p>
                      <div className="flex items-center gap-3">
                        <StatusBadge status={item.status} />
                        {item.mccCategoryCode && (
                          <span className="text-xs text-zinc-500">
                            {getMccLabel(item.mccCategoryCode)}
                          </span>
                        )}
                      </div>
                    </div>
                    {item.relevance !== undefined && item.relevance > 0 && (
                      <div className="text-right">
                        <span className="text-xs text-citrus">Relevância: {Math.round(item.relevance * 100)}%</span>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex justify-center gap-2 mt-8">
                <Button
                  variant="outline"
                  disabled={page === 0}
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                >
                  Anterior
                </Button>
                <span className="px-4 py-2 text-zinc-400">
                  {page + 1} / {totalPages}
                </span>
                <Button
                  variant="outline"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage(p => p + 1)}
                >
                  Próxima
                </Button>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}