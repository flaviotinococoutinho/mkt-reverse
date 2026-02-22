import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { AppHeader } from '../../components/layout/AppHeader';
import { sourcingService } from '../../services/sourcingService';
import type { SourcingEventView, SupplierResponseView } from '../../services/sourcingService';
import { CheckCircle, Clock, Users, DollarSign, Truck, Shield } from 'lucide-react';
import { useToast } from '../../context/useToast';
import { getConditionLabel, getShippingModeLabel } from '../../lib/offerTerms';
import { formatBrlFromCents } from '../../lib/currency';

export default function SourcingEventDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [event, setEvent] = useState<SourcingEventView | null>(null);
  const [responses, setResponses] = useState<SupplierResponseView[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [acceptingResponseId, setAcceptingResponseId] = useState<string | null>(null);
  const [selectedResponseStatus, setSelectedResponseStatus] = useState<'ALL' | 'SUBMITTED' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN'>('ALL');

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
    }, 30000);

    return () => window.clearInterval(intervalId);
  }, [loadResponses]);

  const handleAcceptResponse = async (responseId: string) => {
    if (!confirm('Tem certeza que deseja aceitar esta proposta?')) {
      return;
    }

    setAcceptingResponseId(responseId);
    try {
      await sourcingService.acceptResponse(id!, responseId);
      toast({
        level: 'success',
        title: 'Proposta aceita',
        description: 'A proposta foi marcada como vencedora e o pedido avançou de status.',
      });
      loadResponses();
      loadEvent();
    } catch (error) {
      console.error('Failed to accept response:', error);
      toast({
        level: 'error',
        title: 'Erro ao aceitar proposta',
        description: 'Tente novamente. Se persistir, verifique o backend e os logs.',
      });
    } finally {
      setAcceptingResponseId(null);
    }
  };

  const visibleResponses = responses.filter((response) => {
    if (selectedResponseStatus === 'ALL') {
      return true;
    }

    return response.status === selectedResponseStatus;
  });

  const hasActiveFilters = selectedResponseStatus !== 'ALL';

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
            <h1 className="text-3xl font-serif text-zinc-100 mb-2">{event.title}</h1>
            <p className="text-zinc-400">{event.description || 'Sem descrição'}</p>
          </div>

          {/* Responses Section */}
          <div className="space-y-6">
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
                {responses.length === 0 && event.status === 'PUBLISHED' && (
                  <Button variant="secondary" onClick={() => navigate('/create-request')}>
                    Editar Solicitação
                  </Button>
                )}
              </div>
            </div>

            <div className="auction-panel p-4">
              <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                <div className="flex flex-wrap items-center gap-2">
                  <label htmlFor="response-status" className="text-sm text-zinc-400">
                    Status da proposta:
                  </label>
                  <select
                    id="response-status"
                    value={selectedResponseStatus}
                    onChange={(event) =>
                      setSelectedResponseStatus(
                        event.target.value as 'ALL' | 'SUBMITTED' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN'
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

                {hasActiveFilters && (
                  <button
                    type="button"
                    onClick={() => setSelectedResponseStatus('ALL')}
                    className="text-sm text-citrus hover:text-citrus/80 underline underline-offset-2"
                  >
                    Limpar filtro
                  </button>
                )}
              </div>

              {hasActiveFilters && (
                <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-zinc-400">
                  <span className="text-zinc-500">Filtro ativo:</span>
                  <span className="rounded-full border border-stroke px-2 py-1 text-zinc-300">
                    status: {selectedResponseStatus}
                  </span>
                </div>
              )}
            </div>

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
                    className="auction-panel p-6 hover:border-stroke/50 transition-colors"
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
                      {response.status === 'SUBMITTED' && event.status === 'PUBLISHED' && (
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
