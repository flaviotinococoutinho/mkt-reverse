import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { AppHeader } from '../../components/layout/AppHeader';
import { PostAcceptanceSummary } from '../../components/buyer/PostAcceptanceSummary';
import { sourcingService } from '../../services/sourcingService';
import type { SourcingEventView, SupplierResponseView } from '../../services/sourcingService';
import { CheckCircle, Clock, Users, DollarSign, Truck, Shield } from 'lucide-react';
import { useToast } from '../../context/useToast';
import { getConditionLabel, getShippingModeLabel } from '../../lib/offerTerms';
import { formatBrlFromCents } from '../../lib/currency';
import { filterAndSortResponses } from '../../lib/responseFilters';
import { acceptResponseAndRefresh } from '../../lib/acceptResponseFlow';
import { toggleComparisonSelection } from '../../lib/responseComparison';
import {
  parseResponseDetailPreferences,
  toResponseDetailPreferencesQueryParams,
  type ResponseSortMode,
  type ResponseStatusFilter,
} from '../../lib/responseDetailPreferences';

export default function SourcingEventDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { toast } = useToast();
  const initialPreferences = parseResponseDetailPreferences(searchParams);
  const [event, setEvent] = useState<SourcingEventView | null>(null);
  const [responses, setResponses] = useState<SupplierResponseView[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [acceptingResponseId, setAcceptingResponseId] = useState<string | null>(null);
  const [selectedResponseStatus, setSelectedResponseStatus] = useState<ResponseStatusFilter>(initialPreferences.status);
  const [selectedSortMode, setSelectedSortMode] = useState<ResponseSortMode>(initialPreferences.sortBy);
  const [maxOfferInput, setMaxOfferInput] = useState(initialPreferences.maxOfferInput);
  const [favoriteResponseIds, setFavoriteResponseIds] = useState<string[]>(initialPreferences.favoriteResponseIds);
  const [comparisonIds, setComparisonIds] = useState<string[]>(initialPreferences.comparisonIds);
  const [showOnlyFavorites, setShowOnlyFavorites] = useState(initialPreferences.showOnlyFavorites);
  const [isEditingEvent, setIsEditingEvent] = useState(false);
  const [editTitle, setEditTitle] = useState('');
  const [editDescription, setEditDescription] = useState('');
  const [savingEvent, setSavingEvent] = useState(false);

  const loadEvent = useCallback(async () => {
    try {
      const data = await sourcingService.getSourcingEvent(id!);
      setEvent(data);
    } catch (error) {
      console.error('Failed to load event:', error);
    }
  }, [id]);

  const loadResponses = useCallback(async (silent = false) => {
    if (!silent) {
      setRefreshing(true);
    }

    try {
      const data = await sourcingService.getResponses(id!);
      setResponses(data);
    } catch (error) {
      console.error('Failed to load responses:', error);
    } finally {
      setLoading(false);
      if (!silent) {
        setRefreshing(false);
      }
    }
  }, [id]);

  useEffect(() => {
    void loadEvent();
    void loadResponses();
  }, [loadEvent, loadResponses]);

  useEffect(() => {
    const intervalId = window.setInterval(() => {
      void loadResponses(true);
      void loadEvent();
    }, 30000);

    return () => window.clearInterval(intervalId);
  }, [loadEvent, loadResponses]);

  useEffect(() => {
    const nextParams = toResponseDetailPreferencesQueryParams({
      status: selectedResponseStatus,
      sortBy: selectedSortMode,
      maxOfferInput,
      favoriteResponseIds,
      comparisonIds,
      showOnlyFavorites,
    });

    const current = searchParams.toString();
    const next = nextParams.toString();
    if (current !== next) {
      setSearchParams(nextParams, { replace: true });
    }
  }, [comparisonIds, favoriteResponseIds, maxOfferInput, searchParams, selectedResponseStatus, selectedSortMode, setSearchParams, showOnlyFavorites]);

  const handleAcceptResponse = async (responseId: string) => {
    if (!confirm('Tem certeza que deseja aceitar esta proposta?')) {
      return;
    }

    setAcceptingResponseId(responseId);
    try {
      await acceptResponseAndRefresh(
        {
          acceptResponse: sourcingService.acceptResponse,
          loadResponses,
          loadEvent,
          onSuccess: () => {
            toast({
              level: 'success',
              title: 'Proposta aceita',
              description: 'A proposta foi marcada como vencedora e o pedido avançou de status.',
            });
          },
          onError: () => {
            toast({
              level: 'error',
              title: 'Erro ao aceitar proposta',
              description: 'Tente novamente. Se persistir, verifique o backend e os logs.',
            });
          },
        },
        { eventId: id!, responseId }
      );
    } catch (error) {
      console.error('Failed to accept response:', error);
    } finally {
      setAcceptingResponseId(null);
    }
  };

  const normalizedMaxOffer = maxOfferInput.trim();
  const parsedMaxOffer = normalizedMaxOffer
    ? Math.round(Number.parseFloat(normalizedMaxOffer.replace(',', '.')) * 100)
    : undefined;
  const maxOfferCents = Number.isFinite(parsedMaxOffer) ? parsedMaxOffer : undefined;

  const filteredResponses = filterAndSortResponses(responses, {
    status: selectedResponseStatus,
    sortBy: selectedSortMode,
    maxOfferCents,
  });

  const visibleResponses = showOnlyFavorites
    ? filteredResponses.filter((response) => favoriteResponseIds.includes(response.id))
    : filteredResponses;

  const hasActiveFilters =
    selectedResponseStatus !== 'ALL' ||
    Boolean(normalizedMaxOffer) ||
    selectedSortMode !== 'BEST_PRICE' ||
    showOnlyFavorites;
  const canAcceptResponse = event?.status === 'PUBLISHED' || event?.status === 'IN_PROGRESS';
  const acceptedResponse = responses.find((response) => response.status === 'ACCEPTED');
  const comparisonResponses = comparisonIds
    .map((comparisonId) => responses.find((response) => response.id === comparisonId))
    .filter((response): response is SupplierResponseView => Boolean(response));

  const toggleFavorite = (responseId: string) => {
    setFavoriteResponseIds((currentIds) =>
      currentIds.includes(responseId)
        ? currentIds.filter((id) => id !== responseId)
        : [...currentIds, responseId]
    );
  };

  const toggleComparison = (responseId: string) => {
    setComparisonIds((currentIds) => {
      const { nextIds, limitReached } = toggleComparisonSelection(currentIds, responseId);

      if (limitReached) {
        toast({
          level: 'info',
          title: 'Comparação limitada a 2 propostas',
          description: 'Remova uma proposta da comparação para adicionar outra.',
        });
      }

      return nextIds;
    });
  };

  const startEditingEvent = () => {
    setEditTitle(event?.title ?? '');
    setEditDescription(event?.description ?? '');
    setIsEditingEvent(true);
  };

  const cancelEditingEvent = () => {
    setIsEditingEvent(false);
    setEditTitle(event?.title ?? '');
    setEditDescription(event?.description ?? '');
  };

  const handleSaveEvent = async () => {
    const nextTitle = editTitle.trim();
    if (!nextTitle) {
      toast({
        level: 'error',
        title: 'Título obrigatório',
        description: 'Informe um título para salvar a solicitação.',
      });
      return;
    }

    setSavingEvent(true);
    try {
      await sourcingService.updateSourcingEvent(id!, {
        tenantId: event?.tenantId,
        title: nextTitle,
        description: editDescription.trim() || undefined,
      });
      await loadEvent();
      setIsEditingEvent(false);
      toast({
        level: 'success',
        title: 'Solicitação atualizada',
        description: 'Título e descrição foram atualizados com sucesso.',
      });
    } catch (error) {
      console.error('Failed to update event:', error);
      toast({
        level: 'error',
        title: 'Erro ao atualizar solicitação',
        description: 'Tente novamente. Se persistir, verifique os logs do backend.',
      });
    } finally {
      setSavingEvent(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-ink flex items-center justify-center">
        <div className="text-zinc-400">Carregando...</div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="min-h-screen bg-ink flex items-center justify-center">
        <div className="text-zinc-400">Solicitação não encontrada</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-ink text-zinc-200 font-sans">
      <AppHeader backTo="/dashboard" backLabel="Dashboard" />

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          {/* Event Header */}
          <div className="mb-8 pb-6 border-b border-stroke">
            <div className="flex items-center gap-3 mb-4">
              <StatusBadge status={event.status} size="md" />
            </div>
            {isEditingEvent ? (
              <div className="space-y-3">
                <label className="block">
                  <span className="mb-1 block text-sm text-zinc-400">Título</span>
                  <input
                    type="text"
                    value={editTitle}
                    onChange={(event) => setEditTitle(event.target.value)}
                    className="w-full rounded-md border border-stroke bg-ink px-3 py-2 text-zinc-200 focus:outline-none focus:ring-2 focus:ring-citrus/40"
                    maxLength={200}
                  />
                </label>

                <label className="block">
                  <span className="mb-1 block text-sm text-zinc-400">Descrição</span>
                  <textarea
                    value={editDescription}
                    onChange={(event) => setEditDescription(event.target.value)}
                    rows={4}
                    className="w-full rounded-md border border-stroke bg-ink px-3 py-2 text-zinc-200 focus:outline-none focus:ring-2 focus:ring-citrus/40"
                    maxLength={1000}
                  />
                </label>

                <div className="flex gap-2">
                  <Button onClick={() => void handleSaveEvent()} isLoading={savingEvent}>
                    Salvar alterações
                  </Button>
                  <Button variant="outline" onClick={cancelEditingEvent} disabled={savingEvent}>
                    Cancelar
                  </Button>
                </div>
              </div>
            ) : (
              <>
                <h1 className="text-3xl font-serif text-zinc-100 mb-2">{event.title}</h1>
                <p className="text-zinc-400">{event.description || 'Sem descrição'}</p>
              </>
            )}
          </div>

          {/* Responses Section */}
          <div className="space-y-6">
            {acceptedResponse && (
              <PostAcceptanceSummary
                acceptedResponse={acceptedResponse}
                eventStatus={event.status}
                onGoToDashboard={() => navigate('/dashboard')}
                onCreateRequest={() => navigate('/create-request')}
              />
            )}

            <div className="flex justify-between items-center">
              <h2 className="text-2xl font-serif text-zinc-100">
                Propostas Recebidas ({visibleResponses.length}/{responses.length})
              </h2>
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  onClick={() => void loadResponses()}
                  isLoading={refreshing}
                >
                  Atualizar propostas
                </Button>
                {responses.length === 0 && canAcceptResponse && (
                  <Button variant="secondary" onClick={startEditingEvent} disabled={isEditingEvent}>
                    Editar título/descrição
                  </Button>
                )}
              </div>
            </div>

            <div className="auction-panel p-4">
              <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                <div className="grid gap-3 md:grid-cols-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <label htmlFor="response-status" className="text-sm text-zinc-400">
                      Status:
                    </label>
                    <select
                      id="response-status"
                      value={selectedResponseStatus}
                      onChange={(event) =>
                        setSelectedResponseStatus(
                          event.target.value as ResponseStatusFilter
                        )
                      }
                      className="rounded-md border border-stroke bg-ink px-3 py-2 text-sm text-zinc-200 focus:outline-none focus:ring-2 focus:ring-citrus/40"
                    >
                      <option value="ALL">Todas</option>
                      <option value="SUBMITTED">Enviadas</option>
                      <option value="ACCEPTED">Aceitas</option>
                      <option value="REJECTED">Rejeitadas</option>
                      <option value="WITHDRAWN">Retiradas</option>
                    </select>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    <label htmlFor="response-sort" className="text-sm text-zinc-400">
                      Ordenação:
                    </label>
                    <select
                      id="response-sort"
                      value={selectedSortMode}
                      onChange={(event) =>
                        setSelectedSortMode(event.target.value as ResponseSortMode)
                      }
                      className="rounded-md border border-stroke bg-ink px-3 py-2 text-sm text-zinc-200 focus:outline-none focus:ring-2 focus:ring-citrus/40"
                    >
                      <option value="BEST_PRICE">Menor preço</option>
                      <option value="FASTEST_DELIVERY">Menor prazo</option>
                      <option value="NEWEST">Mais recentes</option>
                    </select>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    <label htmlFor="response-max-offer" className="text-sm text-zinc-400">
                      Preço máx (R$):
                    </label>
                    <input
                      id="response-max-offer"
                      type="number"
                      min={0}
                      step="0.01"
                      value={maxOfferInput}
                      onChange={(event) => setMaxOfferInput(event.target.value)}
                      placeholder="Ex: 1200"
                      className="w-32 rounded-md border border-stroke bg-ink px-3 py-2 text-sm text-zinc-200 focus:outline-none focus:ring-2 focus:ring-citrus/40"
                    />
                  </div>

                  <label className="flex items-center gap-2 text-sm text-zinc-300">
                    <input
                      type="checkbox"
                      checked={showOnlyFavorites}
                      onChange={(event) => setShowOnlyFavorites(event.target.checked)}
                      className="h-4 w-4 rounded border-stroke bg-ink text-citrus focus:ring-citrus/40"
                    />
                    Apenas favoritos
                  </label>
                </div>

                {hasActiveFilters && (
                  <button
                    type="button"
                    onClick={() => {
                      setSelectedResponseStatus('ALL');
                      setSelectedSortMode('BEST_PRICE');
                      setMaxOfferInput('');
                      setShowOnlyFavorites(false);
                    }}
                    className="text-sm text-citrus hover:text-citrus/80 underline underline-offset-2"
                  >
                    Limpar filtros
                  </button>
                )}
              </div>

              {hasActiveFilters && (
                <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-zinc-400">
                  <span className="text-zinc-500">Filtros ativos:</span>
                  {selectedResponseStatus !== 'ALL' && (
                    <span className="rounded-full border border-stroke px-2 py-1 text-zinc-300">
                      status: {selectedResponseStatus}
                    </span>
                  )}
                  {selectedSortMode !== 'BEST_PRICE' && (
                    <span className="rounded-full border border-stroke px-2 py-1 text-zinc-300">
                      ordenação: {selectedSortMode === 'FASTEST_DELIVERY' ? 'menor prazo' : 'mais recentes'}
                    </span>
                  )}
                  {normalizedMaxOffer && (
                    <span className="rounded-full border border-stroke px-2 py-1 text-zinc-300">
                      até: {formatBrlFromCents(maxOfferCents ?? 0)}
                    </span>
                  )}
                  {showOnlyFavorites && (
                    <span className="rounded-full border border-stroke px-2 py-1 text-zinc-300">
                      somente favoritos
                    </span>
                  )}
                </div>
              )}
            </div>

            {comparisonResponses.length > 0 && (
              <div className="auction-panel p-4">
                <div className="mb-3 flex items-center justify-between">
                  <h3 className="text-sm uppercase tracking-wide text-zinc-400">Comparação rápida</h3>
                  <button
                    type="button"
                    onClick={() => setComparisonIds([])}
                    className="text-xs text-citrus hover:text-citrus/80 underline underline-offset-2"
                  >
                    Limpar comparação
                  </button>
                </div>
                <div className="grid gap-3 md:grid-cols-2">
                  {comparisonResponses.map((response) => (
                    <div key={`comparison-${response.id}`} className="rounded-md border border-stroke p-3">
                      <div className="flex items-center justify-between gap-2">
                        <p className="text-xs text-zinc-500">Proposta #{response.id.slice(0, 8)}</p>
                        <StatusBadge status={response.status} size="sm" />
                      </div>
                      <p className="mt-1 text-xl font-semibold text-zinc-100">
                        {formatBrlFromCents(response.offerCents)}
                      </p>
                      <p className="mt-2 text-sm text-zinc-400">
                        Prazo: {response.leadTimeDays ? `${response.leadTimeDays} dias` : 'não informado'}
                      </p>
                      <p className="text-sm text-zinc-400">
                        Garantia: {response.warrantyMonths ? `${response.warrantyMonths} meses` : 'não informada'}
                      </p>

                      {response.status === 'SUBMITTED' && canAcceptResponse && (
                        <Button
                          className="mt-3 w-full"
                          isLoading={acceptingResponseId === response.id}
                          onClick={() => handleAcceptResponse(response.id)}
                        >
                          <CheckCircle className="mr-2 h-4 w-4" />
                          Aceitar desta comparação
                        </Button>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {responses.length === 0 ? (
              <div className="auction-panel text-center py-12">
                <Users className="h-16 w-16 mx-auto mb-4 text-zinc-600" />
                <p className="text-zinc-400 mb-2">Aguardando propostas</p>
                <p className="text-sm text-zinc-500">
                  As propostas dos fornecedores aparecerão aqui
                </p>
              </div>
            ) : visibleResponses.length === 0 ? (
              <div className="auction-panel text-center py-12">
                <Users className="h-16 w-16 mx-auto mb-4 text-zinc-600" />
                <p className="text-zinc-400 mb-2">Nenhuma proposta neste filtro</p>
                <p className="text-sm text-zinc-500">
                  Ajuste o filtro de status para visualizar outras propostas
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {visibleResponses.map((response) => (
                  <div
                    key={response.id}
                    className={`auction-panel p-6 transition-colors ${
                      response.status === 'ACCEPTED'
                        ? 'border-emerald-500/40 bg-emerald-500/5'
                        : 'hover:border-stroke/50'
                    }`}
                  >
                    <div className="flex justify-between items-start mb-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <DollarSign className="h-5 w-5 text-citrus" />
                          <span className="text-2xl font-bold text-zinc-100">
                            {formatBrlFromCents(response.offerCents)}
                          </span>
                        </div>
                        <StatusBadge status={response.status} />
                      </div>
                      <div className="text-right ml-4">
                        <p className="text-xs text-zinc-500 mb-1">ID da Proposta</p>
                        <p className="text-sm font-mono text-zinc-400">{response.id.slice(0, 8)}...</p>
                      </div>
                    </div>

                    <div className="mb-4 flex flex-wrap items-center gap-3 text-sm">
                      <button
                        type="button"
                        onClick={() => toggleFavorite(response.id)}
                        className={`rounded-md border px-3 py-1 transition-colors ${
                          favoriteResponseIds.includes(response.id)
                            ? 'border-citrus/40 bg-citrus/10 text-citrus'
                            : 'border-stroke text-zinc-300 hover:border-citrus/30 hover:text-citrus'
                        }`}
                      >
                        {favoriteResponseIds.includes(response.id) ? '★ Favorita' : '☆ Favoritar'}
                      </button>

                      <label className="flex items-center gap-2 text-zinc-300">
                        <input
                          type="checkbox"
                          checked={comparisonIds.includes(response.id)}
                          onChange={() => toggleComparison(response.id)}
                          className="h-4 w-4 rounded border-stroke bg-ink text-citrus focus:ring-citrus/40"
                        />
                        Comparar
                      </label>
                    </div>

                    {/* Response Details */}
                    <div className="grid grid-cols-2 gap-4 mb-4">
                      {response.leadTimeDays && (
                        <div className="flex items-center gap-2">
                          <Clock className="h-4 w-4 text-zinc-500" />
                          <span className="text-sm text-zinc-400">
                            Prazo: {response.leadTimeDays} dias
                          </span>
                        </div>
                      )}
                      {response.warrantyMonths && (
                        <div className="flex items-center gap-2">
                          <Shield className="h-4 w-4 text-zinc-500" />
                          <span className="text-sm text-zinc-400">
                            Garantia: {response.warrantyMonths} meses
                          </span>
                        </div>
                      )}
                      {response.condition && (
                        <div className="flex items-center gap-2">
                          <Shield className="h-4 w-4 text-zinc-500" />
                          <span className="text-sm text-zinc-400">
                            Condição: {getConditionLabel(response.condition)}
                          </span>
                        </div>
                      )}
                      {response.shippingMode && (
                        <div className="flex items-center gap-2">
                          <Truck className="h-4 w-4 text-zinc-500" />
                          <span className="text-sm text-zinc-400">
                            Frete: {getShippingModeLabel(response.shippingMode)}
                          </span>
                        </div>
                      )}
                    </div>

                    {/* Message */}
                    {response.message && (
                      <div className="auction-panel mb-4 p-3 rounded-md">
                        <p className="text-sm text-zinc-400 italic">"{response.message}"</p>
                      </div>
                    )}

                    {/* Action Buttons */}
                    <div className="flex gap-3">
                      {response.status === 'SUBMITTED' && canAcceptResponse && (
                        <Button
                          className="flex-1"
                          isLoading={acceptingResponseId === response.id}
                          onClick={() => handleAcceptResponse(response.id)}
                        >
                          <CheckCircle className="mr-2 h-4 w-4" />
                          Aceitar Proposta
                        </Button>
                      )}
                      {response.status === 'ACCEPTED' && (
                        <Button variant="secondary" className="flex-1" disabled>
                          <CheckCircle className="mr-2 h-4 w-4" />
                          Proposta Aceita
                        </Button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
