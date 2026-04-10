import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, FileText, DollarSign, AlertCircle } from 'lucide-react';

import { AppHeader } from '../../components/layout/AppHeader';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { sourcingService } from '../../services/sourcingService';
import type { SourcingEventView } from '../../services/sourcingService';
import { getEventTypeLabel } from '../../lib/eventType';

export default function OpportunityDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [event, setEvent] = useState<SourcingEventView | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await sourcingService.getSourcingEvent(id);
      setEvent(data);
    } catch (error) {
      console.error('Failed to load opportunity:', error);
      setEvent(null);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void load();
  }, [load]);

  if (loading) {
    return (
      <div className="min-h-screen bg-ink text-zinc-200 font-sans">
        <AppHeader backTo="/supplier/opportunities" backLabel="Oportunidades" />
        <main className="container mx-auto px-4 py-10">
          <div className="text-zinc-400">Carregando...</div>
        </main>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="min-h-screen bg-ink text-zinc-200 font-sans">
        <AppHeader backTo="/supplier/opportunities" backLabel="Oportunidades" />
        <main className="container mx-auto px-4 py-10">
          <div className="auction-panel mx-auto max-w-xl p-6 text-center">
            <AlertCircle className="mx-auto h-14 w-14 text-zinc-600" aria-hidden="true" />
            <div className="mt-4 text-zinc-300">Solicitação não encontrada</div>
            <div className="mt-2 text-sm text-zinc-500">Pode ter expirado, ou você não tem acesso.</div>
            <div className="mt-5">
              <Button onClick={() => navigate('/supplier/opportunities')}>Voltar</Button>
            </div>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-ink text-zinc-200 font-sans">
      <AppHeader backTo="/supplier/opportunities" backLabel="Oportunidades" />

      <main className="container mx-auto px-4 py-8">
        <div className="mx-auto max-w-3xl">
          <div className="mb-8">
            <div className="flex items-center gap-2 text-xs text-zinc-500 font-mono">
              <span className="inline-flex items-center gap-1 rounded-md border border-stroke bg-paper px-2 py-1">
                <FileText className="h-3.5 w-3.5 text-zinc-300" aria-hidden="true" />
                OPPORTUNITY
              </span>
              <span className="truncate">{event.id}</span>
            </div>
            <h1 className="mt-4 text-3xl font-serif text-zinc-100">{event.title}</h1>
            <p className="mt-2 text-zinc-400 leading-relaxed">{event.description || 'Sem descrição detalhada.'}</p>
          </div>

          <div className="grid gap-4 md:grid-cols-3">
            <div className="auction-panel p-5">
              <div className="text-xs text-zinc-500">Tipo</div>
              <div className="mt-1 text-sm text-zinc-200">{getEventTypeLabel(event.eventType)}</div>
            </div>
            <div className="auction-panel p-5">
              <div className="text-xs text-zinc-500">Status</div>
              <div className="mt-1">
                <StatusBadge status={event.status} />
              </div>
            </div>
            <div className="auction-panel p-5">
              <div className="text-xs text-zinc-500">Próximo passo</div>
              <div className="mt-1 text-sm text-zinc-200">Enviar uma proposta</div>
            </div>
          </div>

          <div className="auction-panel mt-8 p-6 rounded-2xl">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <div className="font-serif text-lg text-zinc-100">Pronto para ofertar?</div>
                <div className="mt-1 text-sm text-zinc-400">
                  Envie preço, prazo e condições. O comprador verá sua proposta no painel.
                </div>
              </div>
              <div className="flex gap-3">
                <Button
                  variant="outline"
                  onClick={() => navigate('/supplier/opportunities')}
                >
                  <ArrowLeft className="mr-2 h-4 w-4" aria-hidden="true" />
                  Voltar
                </Button>
                <Button onClick={() => navigate(`/supplier/submit-proposal/${event.id}`)}>
                  <DollarSign className="mr-2 h-4 w-4" aria-hidden="true" />
                  Enviar proposta
                </Button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
