import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { AppHeader } from '../../components/layout/AppHeader';
import { useAuth } from '../../context/useAuth';
import { sourcingService } from '../../services/sourcingService';
import type { SourcingEventView } from '../../services/sourcingService';
import { getEventTypeLabel } from '../../lib/eventType';
import {
  OPPORTUNITY_PAGE_SIZE_OPTIONS,
  OPPORTUNITY_SORT_BY_OPTIONS,
  OPPORTUNITY_SORT_DIR_OPTIONS,
  OPPORTUNITY_VISIBILITY_OPTIONS,
  parseOpportunityQueryParams,
  toOpportunityQueryParams,
  type OpportunityPageSize,
  type OpportunitySortBy,
  type OpportunitySortDir,
  type OpportunityVisibility,
} from '../../lib/opportunityDiscovery';
import {
  Search,
  Filter,
  FileText,
  AlertCircle,
  DollarSign,
} from 'lucide-react';
import axios from 'axios';
import { getFriendlyHttpErrorMessage } from '../../lib/problemDetails';

export default function OpportunitiesPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { user } = useAuth();
  const [opportunities, setOpportunities] = React.useState<SourcingEventView[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [loadError, setLoadError] = React.useState<string | null>(null);
  const initialState = React.useMemo(() => parseOpportunityQueryParams(searchParams), [searchParams]);

  const [searchQuery, setSearchQuery] = React.useState(initialState.q);
  const [appliedSearchQuery, setAppliedSearchQuery] = React.useState(initialState.q);
  const [currentPage, setCurrentPage] = React.useState(initialState.page);
  const [pageSize, setPageSize] = React.useState<OpportunityPageSize>(
    initialState.size,
  );
  const [totalPages, setTotalPages] = React.useState(0);
  const [totalItems, setTotalItems] = React.useState(0);
  const [filters, setFilters] = React.useState({
    mccCategoryCode: initialState.mccCategoryCode,
    visibility: initialState.visibility as OpportunityVisibility,
    sortBy: initialState.sortBy as OpportunitySortBy,
    sortDir: initialState.sortDir as OpportunitySortDir,
  });

  const hasActiveFilters =
    appliedSearchQuery.length > 0 ||
    filters.mccCategoryCode.trim().length > 0 ||
    filters.visibility !== 'ALL' ||
    filters.sortBy !== 'PUBLICATION_AT' ||
    filters.sortDir !== 'DESC';

  const clearFilters = () => {
    setSearchQuery('');
    setAppliedSearchQuery('');
    setFilters({
      mccCategoryCode: '',
      visibility: 'ALL',
      sortBy: 'PUBLICATION_AT',
      sortDir: 'DESC',
    });
    setCurrentPage(0);
    setPageSize(OPPORTUNITY_PAGE_SIZE_OPTIONS[0]);
  };

  const loadOpportunities = React.useCallback(async () => {
    setLoading(true);
    setLoadError(null);

    try {
      const params: Parameters<typeof sourcingService.getOpportunities>[0] = {
        tenantId: user?.tenantId,
        supplierId: user?.id,
        q: appliedSearchQuery || undefined,
        page: currentPage,
        size: pageSize,
        sortBy: filters.sortBy,
        sortDir: filters.sortDir,
        visibility: filters.visibility,
      };

      const normalizedMcc = filters.mccCategoryCode.trim();
      if (normalizedMcc) {
        if (!/^\d+$/.test(normalizedMcc)) {
          setLoadError('Código MCC inválido. Use apenas números.');
          setOpportunities([]);
          setTotalItems(0);
          setTotalPages(0);
          setLoading(false);
          return;
        }
        params.mccCategoryCode = Number.parseInt(normalizedMcc, 10);
      }

      const result = await sourcingService.getOpportunities(params);
      setOpportunities(result.items);
      setTotalItems(result.total);
      setTotalPages(Math.max(1, Math.ceil(result.total / pageSize)));
    } catch (error) {
      console.error('Failed to load opportunities:', error);
      setOpportunities([]);
      setTotalItems(0);
      setTotalPages(0);

      const fallbackMessage = 'Não foi possível carregar oportunidades no momento. Tente novamente.';
      if (axios.isAxiosError(error)) {
        setLoadError(
          getFriendlyHttpErrorMessage(error.response?.status, error.response?.data)
            ?? error.message
            ?? fallbackMessage,
        );
      } else {
        setLoadError(fallbackMessage);
      }
    } finally {
      setLoading(false);
    }
  }, [appliedSearchQuery, currentPage, filters.mccCategoryCode, filters.sortBy, filters.sortDir, filters.visibility, pageSize, user?.id, user?.tenantId]);

  React.useEffect(() => {
    void loadOpportunities();
  }, [loadOpportunities]);

  React.useEffect(() => {
    const nextParams = toOpportunityQueryParams({
      q: appliedSearchQuery,
      mccCategoryCode: filters.mccCategoryCode,
      visibility: filters.visibility,
      sortBy: filters.sortBy,
      sortDir: filters.sortDir,
      page: currentPage,
      size: pageSize,
    });

    const current = searchParams.toString();
    const next = nextParams.toString();
    if (current !== next) {
      setSearchParams(nextParams, { replace: true });
    }
  }, [appliedSearchQuery, currentPage, filters.mccCategoryCode, filters.sortBy, filters.sortDir, filters.visibility, pageSize, searchParams, setSearchParams]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setAppliedSearchQuery(searchQuery.trim());
    setCurrentPage(0);
  };

  React.useEffect(() => {
    setCurrentPage(0);
  }, [filters.mccCategoryCode, filters.sortBy, filters.sortDir, filters.visibility]);

  React.useEffect(() => {
    setCurrentPage(0);
  }, [pageSize]);

  return (
    <div className="min-h-screen bg-ink text-zinc-200 font-sans">
      <AppHeader backTo="/supplier/dashboard" backLabel="Dashboard" />

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        {/* Page Title */}
        <div className="mb-8">
          <h2 className="text-3xl font-serif text-zinc-100 mb-2">Descobrir Oportunidades</h2>
          <p className="text-zinc-400">Encontre solicitações de compradores relevantes para seu negócio</p>
        </div>

        {/* Search and Filters */}
        <div className="auction-panel p-6 mb-8">
          <form onSubmit={handleSearch} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-zinc-500" />
                <Input
                  type="text"
                  placeholder="Buscar por palavras-chave..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>

              <div className="relative">
                <Filter className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-zinc-500" />
                <Input
                  type="text"
                  inputMode="numeric"
                  placeholder="Código MCC (opcional)"
                  value={filters.mccCategoryCode}
                  onChange={(e) =>
                    setFilters({
                      ...filters,
                      mccCategoryCode: e.target.value.replace(/\D/g, '').slice(0, 4),
                    })
                  }
                  className="pl-10"
                />
              </div>

              <div>
                <label className="block text-sm text-zinc-400 mb-2">Visibilidade</label>
                <select
                  value={filters.visibility}
                  onChange={(e) =>
                    setFilters({ ...filters, visibility: e.target.value as OpportunityVisibility })
                  }
                  className="w-full bg-ink border border-stroke rounded-md px-3 py-2 text-sm text-zinc-200 focus:border-citrus focus:outline-none"
                >
                  {OPPORTUNITY_VISIBILITY_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2">
                <label className="text-sm text-zinc-400">Ordenar por:</label>
                <select
                  value={filters.sortBy}
                  onChange={(e) =>
                    setFilters({ ...filters, sortBy: e.target.value as OpportunitySortBy })
                  }
                  className="bg-ink border border-stroke rounded-md px-3 py-2 text-sm text-zinc-200 focus:border-citrus focus:outline-none"
                >
                  {OPPORTUNITY_SORT_BY_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex items-center gap-2">
                <select
                  value={filters.sortDir}
                  onChange={(e) =>
                    setFilters({ ...filters, sortDir: e.target.value as OpportunitySortDir })
                  }
                  className="bg-ink border border-stroke rounded-md px-3 py-2 text-sm text-zinc-200 focus:border-citrus focus:outline-none"
                >
                  {OPPORTUNITY_SORT_DIR_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex-1" />

              <Button type="submit">
                <Search className="mr-2 h-4 w-4" />
                Buscar
              </Button>
            </div>
          </form>
        </div>

        {/* Results */}
        <div className="mb-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm text-zinc-400">
              {loading
                ? 'Carregando...'
                : `${totalItems} oportunidades encontradas`}
            </p>

            <div className="flex items-center gap-2">
              <label htmlFor="opportunities-page-size" className="text-xs text-zinc-500">
                Itens por página
              </label>
              <select
                id="opportunities-page-size"
                value={pageSize}
                onChange={(e) => setPageSize(Number(e.target.value) as OpportunityPageSize)}
                className="bg-ink border border-stroke rounded-md px-2 py-1 text-xs text-zinc-200 focus:border-citrus focus:outline-none"
              >
                {OPPORTUNITY_PAGE_SIZE_OPTIONS.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {hasActiveFilters && (
            <div className="mt-3 flex flex-wrap items-center gap-2">
              <span className="text-xs text-zinc-500">Filtros ativos:</span>
              {appliedSearchQuery && (
                <span className="inline-flex items-center rounded-full border border-citrus/30 bg-citrus/10 px-2 py-1 text-xs text-citrus">
                  Busca: “{appliedSearchQuery}”
                </span>
              )}
              {filters.mccCategoryCode && (
                <span className="inline-flex items-center rounded-full border border-citrus/30 bg-citrus/10 px-2 py-1 text-xs text-citrus">
                  MCC: {filters.mccCategoryCode}
                </span>
              )}
              {filters.visibility !== 'ALL' && (
                <span className="inline-flex items-center rounded-full border border-citrus/30 bg-citrus/10 px-2 py-1 text-xs text-citrus">
                  Visibilidade: {OPPORTUNITY_VISIBILITY_OPTIONS.find((o) => o.value === filters.visibility)?.label}
                </span>
              )}
              {(filters.sortBy !== 'PUBLICATION_AT' || filters.sortDir !== 'DESC') && (
                <span className="inline-flex items-center rounded-full border border-citrus/30 bg-citrus/10 px-2 py-1 text-xs text-citrus">
                  Ordem: {OPPORTUNITY_SORT_BY_OPTIONS.find((o) => o.value === filters.sortBy)?.label} ({OPPORTUNITY_SORT_DIR_OPTIONS.find((o) => o.value === filters.sortDir)?.label})
                </span>
              )}
              <button
                type="button"
                onClick={clearFilters}
                className="text-xs text-zinc-400 underline decoration-zinc-600 underline-offset-4 hover:text-zinc-200"
              >
                Limpar filtros
              </button>
            </div>
          )}
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-zinc-400">Carregando oportunidades...</div>
          </div>
        ) : loadError ? (
          <div className="auction-panel text-center py-12 px-6">
            <p className="text-danger text-sm font-mono mb-2">FALHA_AO_CARREGAR</p>
            <p className="text-zinc-300 mb-6">{loadError}</p>
            <Button
              variant="secondary"
              onClick={() => {
                void loadOpportunities();
              }}
            >
              Tentar novamente
            </Button>
          </div>
        ) : opportunities.length === 0 ? (
          <div className="text-center py-12">
            <AlertCircle className="h-16 w-16 mx-auto mb-4 text-zinc-600" />
            <p className="text-zinc-400 mb-4">
              {hasActiveFilters
                ? 'Nenhuma oportunidade encontrada com os filtros atuais.'
                : 'Ainda não há oportunidades disponíveis para o seu perfil.'}
            </p>
            {hasActiveFilters ? (
              <Button variant="secondary" onClick={clearFilters}>
                Limpar filtros
              </Button>
            ) : (
              <Button onClick={() => navigate('/supplier/dashboard')}>
                Voltar ao Dashboard
              </Button>
            )}
          </div>
        ) : (
          <div className="space-y-4">
            {opportunities.map((event) => (
              <div
                key={event.id}
                className="auction-panel p-6 hover:border-stroke/50 transition-colors"
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="inline-flex items-center gap-1 px-2 py-1 rounded-md text-xs font-medium bg-citrus/10 text-citrus">
                        {getEventTypeLabel(event.eventType)}
                      </span>
                      <StatusBadge status={event.status} />
                    </div>
                    <h3 className="text-xl font-serif text-zinc-100 mb-1">{event.title}</h3>
                    <p className="text-sm text-zinc-400 mb-2">
                      {event.description || 'Sem descrição detalhada'}
                    </p>
                  </div>
                  <div className="text-right ml-4">
                    <p className="text-xs text-zinc-500 mb-1">ID da Solicitação</p>
                    <p className="text-sm font-mono text-zinc-400">{event.id.slice(0, 8)}...</p>
                  </div>
                </div>

                <div className="flex items-center gap-4 pt-4 border-t border-stroke">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => navigate(`/supplier/opportunities/${event.id}`)}
                  >
                    <FileText className="mr-2 h-4 w-4" />
                    Ver Detalhes
                  </Button>
                  <Button
                    size="sm"
                    onClick={() => navigate(`/supplier/submit-proposal/${event.id}`)}
                  >
                    <DollarSign className="mr-2 h-4 w-4" />
                    Enviar Proposta
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-4 mt-8">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
              disabled={currentPage === 0}
            >
              Anterior
            </Button>
            <span className="text-sm text-zinc-400">
              Página {currentPage + 1} de {totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={currentPage >= totalPages - 1}
            >
              Próxima
            </Button>
          </div>
        )}
      </main>
    </div>
  );
}
