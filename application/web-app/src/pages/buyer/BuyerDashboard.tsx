import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../context/useAuth';
import { AppHeader } from '../../components/layout/AppHeader';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { sourcingService } from '../../services/sourcingService';
import type { SourcingEventView } from '../../services/sourcingService';
import { Plus, FileText } from 'lucide-react';

export default function BuyerDashboard() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { user } = useAuth();
  const [events, setEvents] = React.useState<SourcingEventView[]>([]);
  const [loading, setLoading] = React.useState(true);
  const initialQuery = searchParams.get('q')?.trim() ?? '';
  const initialStatus = searchParams.get('status');
  const initialPage = Number.parseInt(searchParams.get('page') ?? '0', 10);

  const allowedStatusFilters = ['ALL', 'DRAFT', 'PUBLISHED', 'IN_PROGRESS', 'AWARDED', 'CANCELLED'];
  const parsedInitialStatus =
    initialStatus && allowedStatusFilters.includes(initialStatus) ? initialStatus : 'ALL';

  const [searchQuery, setSearchQuery] = React.useState(initialQuery);
  const [appliedSearchQuery, setAppliedSearchQuery] = React.useState(initialQuery);
  const [statusFilter, setStatusFilter] = React.useState<string>(parsedInitialStatus);
  const [currentPage, setCurrentPage] = React.useState(
    Number.isNaN(initialPage) ? 0 : Math.max(0, initialPage)
  );
  const [pageSize] = React.useState(10);
  const [totalEvents, setTotalEvents] = React.useState(0);

  const loadEvents = React.useCallback(async () => {
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
    } catch (error) {
      console.error('Failed to load events:', error);
    } finally {
      setLoading(false);
    }
  }, [appliedSearchQuery, currentPage, pageSize, statusFilter, user?.tenantId]);

  React.useEffect(() => {
    void loadEvents();
  }, [loadEvents]);

  React.useEffect(() => {
    const nextParams = new URLSearchParams();

    if (appliedSearchQuery) {
      nextParams.set('q', appliedSearchQuery);
    }
    if (statusFilter !== 'ALL') {
      nextParams.set('status', statusFilter);
    }
    if (currentPage > 0) {
      nextParams.set('page', String(currentPage));
    }

    const current = searchParams.toString();
    const next = nextParams.toString();
    if (current !== next) {
      setSearchParams(nextParams, { replace: true });
    }
  }, [appliedSearchQuery, currentPage, searchParams, setSearchParams, statusFilter]);

  const hasActiveFilters = Boolean(appliedSearchQuery) || statusFilter !== 'ALL';

  const handleApplyFilters = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
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

      {/* Main Content */}
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

        <div className="auction-panel p-4 mb-6">
          <form onSubmit={handleApplyFilters} className="grid gap-3 md:grid-cols-[1fr_220px_auto]">
            <input
              type="text"
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              placeholder="Buscar por título ou descrição"
              className="w-full rounded-md border border-stroke bg-zinc-900/50 px-3 py-2 text-sm text-zinc-100 placeholder:text-zinc-500 focus:border-citrus focus:outline-none"
            />

            <select
              value={statusFilter}
              onChange={(event) => {
                setLoading(true);
                setCurrentPage(0);
                setStatusFilter(event.target.value);
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

        {/* Events List */}
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-zinc-400">Carregando...</div>
          </div>
        ) : events.length === 0 ? (
          <div className="text-center py-12">
            <FileText className="h-16 w-16 mx-auto mb-4 text-zinc-600" />
            <p className="text-zinc-400 mb-4">Você ainda não tem solicitações</p>
            <Button onClick={() => navigate('/create-request')}>
              Criar Primeira Solicitação
            </Button>
          </div>
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

            <div className="auction-panel flex flex-col gap-3 p-4 md:flex-row md:items-center md:justify-between">
              <p className="text-sm text-zinc-400">
                Mostrando página <span className="text-zinc-200">{currentPage + 1}</span> de{' '}
                <span className="text-zinc-200">{totalPages}</span> • Total no backend:{' '}
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
          </div>
        )}
      </main>
    </div>
  );
}
