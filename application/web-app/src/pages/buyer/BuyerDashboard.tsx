import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../context/useAuth';
import { AppHeader } from '../../components/layout/AppHeader';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { sourcingService } from '../../services/sourcingService';
import type { SourcingEventView } from '../../services/sourcingService';
import { Plus, FileText, Search, AlertCircle } from 'lucide-react';
import axios from 'axios';
import { getFriendlyHttpErrorMessage } from '../../lib/problemDetails';
import { Loading, ListSkeleton, NoData, NoResults } from '../../components/ui/feedback';
import { useToast } from '../../components/ui/feedback';
import {
  parseBuyerDashboardFilters,
  toBuyerDashboardQueryParams,
  type BuyerStatusFilter,
} from '../../lib/buyerDashboardFilters';

export default function BuyerDashboard() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { user } = useAuth();
  const { error: showError } = useToast();

  const [events, setEvents] = React.useState<SourcingEventView[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);

  const initialFilters = parseBuyerDashboardFilters(searchParams);
  const [searchQuery, setSearchQuery] = React.useState(initialFilters.searchQuery);
  const [appliedSearchQuery, setAppliedSearchQuery] = React.useState(initialFilters.searchQuery);
  const [statusFilter, setStatusFilter] = React.useState(initialFilters.status);
  const [currentPage, setCurrentPage] = React.useState(initialFilters.page);
  const [pageSize] = React.useState(10);
  const [totalEvents, setTotalEvents] = React.useState(0);

  const loadEvents = React.useCallback(async () => {
    setError(null);

    try {
      const result = await sourcingService.getSourcingEvents({
        tenantId: user?.tenantId,
        status: statusFilter === 'ALL' ? undefined : statusFilter,
        page: currentPage,
        size: pageSize,
      });

      const normalizedQuery = appliedSearchQuery.trim().toLowerCase();
      const filteredItems = normalizedQuery
        ? result.items.filter((event) => {
            const title = event.title.toLowerCase();
            const description = event.description?.toLowerCase() ?? '';
            return title.includes(normalizedQuery) || description.includes(normalizedQuery);
          })
        : result.items;

      setEvents(filteredItems);
      setTotalEvents(result.total);
    } catch (err) {
      console.error('Failed to load events:', err);
      const fallbackMessage = 'Não foi possível carregar suas solicitações. Tente novamente.';

      let errorMessage = fallbackMessage;
      if (axios.isAxiosError(err)) {
        errorMessage = getFriendlyHttpErrorMessage(err.response?.status, err.response?.data) 
          ?? err.message 
          ?? fallbackMessage;
      }

      setError(errorMessage);
      showError('Erro ao carregar', errorMessage);
    } finally {
      setLoading(false);
    }
  }, [appliedSearchQuery, currentPage, pageSize, statusFilter, user?.tenantId, showError]);

  React.useEffect(() => {
    void loadEvents();
  }, [loadEvents]);

  React.useEffect(() => {
    const nextParams = toBuyerDashboardQueryParams({
      searchQuery: appliedSearchQuery,
      status: statusFilter,
      page: currentPage,
    });

    const current = searchParams.toString();
    const next = nextParams.toString();
    if (current !== next) {
      setSearchParams(nextParams, { replace: true });
    }
  }, [appliedSearchQuery, currentPage, searchParams, setSearchParams, statusFilter]);

  const hasActiveFilters = Boolean(appliedSearchQuery) || statusFilter !== 'ALL';

  const handleApplyFilters = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setCurrentPage(0);
    setAppliedSearchQuery(searchQuery.trim());
  };

  const clearFilters = () => {
    setLoading(true);
    setCurrentPage(0);
    setSearchQuery('');
    setAppliedSearchQuery('');
    setStatusFilter('ALL');
  };

  const totalPages = Math.max(1, Math.ceil(totalEvents / pageSize));
  const isFirstPage = currentPage === 0;
  const isLastPage = currentPage >= totalPages - 1;

  return (
    <div className="min-h-screen bg-ink text-zinc-200 font-sans">
      <AppHeader />

      <main className="container mx-auto px-4 py-8">
        {/* Page Title and Actions */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h2 className="text-3xl font-serif text-zinc-100 mb-2">Meus Pedidos</h2>
            <p className="text-zinc-400">Gerencie suas solicitações e acompanhe as propostas</p>
          </div>
          <Button size="lg" onClick={() => navigate('/create-request')}>
            <Plus className="mr-2 h-5 w-5" />
            Novo Pedido
          </Button>
        </div>

        {/* Filters Panel */}
        <div className="auction-panel p-4 mb-6">
          <form onSubmit={handleApplyFilters} className="grid gap-3 md:grid-cols-[1fr_220px_auto]">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-500" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Buscar por título ou descrição"
                className="w-full pl-10 rounded-md border border-stroke bg-zinc-900/50 px-3 py-2 text-sm text-zinc-100 placeholder:text-zinc-500 focus:border-citrus focus:outline-none"
              />
            </div>

            <select
              value={statusFilter}
              onChange={(e) => {
                setLoading(true);
                setCurrentPage(0);
                setStatusFilter(e.target.value as BuyerStatusFilter);
              }}
              className="w-full rounded-md border border-stroke bg-zinc-900/50 px-3 py-2 text-sm text-zinc-100 focus:border-citrus focus:outline-none"
            >
              <option value="ALL">Todos os status</option>
              <option value="DRAFT">Rascunho</option>
              <option value="PUBLISHED">Publicado</option>
              <option value="IN_PROGRESS">Em andamento</option>
              <option value="AWARDED">Concluído</option>
              <option value="CANCELLED">Cancelado</option>
            </select>

            <Button type="submit" variant="secondary">
              Buscar
            </Button>
          </form>

          {hasActiveFilters && (
            <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-zinc-300">
              <span className="text-zinc-500">Filtros ativos:</span>
              {appliedSearchQuery && (
                <span className="rounded-full border border-stroke px-2 py-1">
                  Busca: "{appliedSearchQuery}"
                </span>
              )}
              {statusFilter !== 'ALL' && (
                <span className="rounded-full border border-stroke px-2 py-1">
                  Status: {statusFilter}
                </span>
              )}
              <button
                type="button"
                onClick={clearFilters}
                className="text-citrus hover:text-citrus/80"
              >
                Limpar filtros
              </button>
            </div>
          )}
        </div>

        {/* Content */}
        {loading ? (
          <div className="auction-panel p-8">
            <ListSkeleton count={5} />
          </div>
        ) : error ? (
          <div className="auction-panel text-center py-12 px-6">
            <AlertCircle className="h-12 w-12 mx-auto mb-4 text-red-500" />
            <p className="text-zinc-300 mb-6">{error}</p>
            <Button
              variant="secondary"
              onClick={() => {
                setLoading(true);
                void loadEvents();
              }}
            >
              Tentar novamente
            </Button>
          </div>
        ) : events.length === 0 ? (
          hasActiveFilters ? (
            <NoResults 
              action={{ 
                label: 'Limpar filtros', 
                onClick: clearFilters 
              }} 
            />
          ) : (
            <NoData 
              message="Você ainda não tem solicitações" 
              action={{ 
                label: 'Criar Primeira Solicitação', 
                onClick: () => navigate('/create-request') 
              }} 
            />
          )
        ) : (
          <div className="space-y-4">
            {events.map((event) => (
              <div
                key={event.id}
                className="auction-panel p-6 hover:border-stroke/50 transition-colors"
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <h3 className="text-xl font-serif text-zinc-100 mb-1">{event.title}</h3>
                    <p className="text-sm text-zinc-400 mb-2">
                      {event.description || 'Sem descrição'}
                    </p>
                    <div className="flex items-center gap-2">
                      <StatusBadge status={event.status} />
                    </div>
                  </div>
                  <div className="text-right ml-4">
                    <p className="text-xs text-zinc-500 mb-1">ID do Pedido</p>
                    <p className="text-sm font-mono text-zinc-400">{event.id.slice(0, 8)}...</p>
                  </div>
                </div>

                <div className="flex items-center gap-4 pt-4 border-t border-stroke">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => navigate(`/sourcing-events/${event.id}`)}
                  >
                    Ver Detalhes
                  </Button>
                  {event.status === 'PUBLISHED' && (
                    <Button variant="secondary" size="sm" onClick={() => navigate(`/create-request`)}>
                      Editar
                    </Button>
                  )}
                </div>
              </div>
            ))}

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="auction-panel flex flex-col gap-3 p-4 md:flex-row md:items-center md:justify-between">
                <p className="text-sm text-zinc-400">
                  Mostrando página <span className="text-zinc-200">{currentPage + 1}</span> de{' '}
                  <span className="text-zinc-200">{totalPages}</span> • Total:{' '}
                  <span className="text-zinc-200">{totalEvents}</span>
                </p>
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={isFirstPage}
                    onClick={() => {
                      setLoading(true);
                      setCurrentPage((page) => Math.max(0, page - 1));
                    }}
                  >
                    Anterior
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={isLastPage}
                    onClick={() => {
                      setLoading(true);
                      setCurrentPage((page) => page + 1);
                    }}
                  >
                    Próxima
                  </Button>
                </div>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}