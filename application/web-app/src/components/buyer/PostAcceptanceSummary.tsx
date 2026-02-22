import { CheckCircle } from 'lucide-react';
import { Button } from '../ui/Button';
import { formatBrlFromCents } from '../../lib/currency';
import { getPostAcceptanceChecklist } from '../../lib/negotiationChecklist';

interface AcceptedResponse {
  id: string;
  offerCents: number;
}

interface PostAcceptanceSummaryProps {
  acceptedResponse: AcceptedResponse;
  eventStatus?: string;
  onGoToDashboard: () => void;
  onCreateRequest: () => void;
}

export function PostAcceptanceSummary({
  acceptedResponse,
  eventStatus,
  onGoToDashboard,
  onCreateRequest,
}: PostAcceptanceSummaryProps) {
  const isEventClosed = eventStatus === 'AWARDED' || eventStatus === 'CLOSED';
  const postAcceptanceChecklist = getPostAcceptanceChecklist(eventStatus);

  return (
    <div className="auction-panel border-emerald-500/30 bg-emerald-500/10 p-4" data-testid="post-acceptance-summary">
      <div className="flex items-start gap-3">
        <CheckCircle className="mt-0.5 h-5 w-5 text-emerald-400" />
        <div>
          <p className="font-semibold text-emerald-300">Proposta vencedora definida</p>
          <p className="text-sm text-emerald-100/80">
            Valor escolhido: {formatBrlFromCents(acceptedResponse.offerCents)} · ID {acceptedResponse.id.slice(0, 8)}...
          </p>
          {isEventClosed && (
            <p className="mt-1 text-xs text-emerald-100/70">Status da solicitação: {eventStatus}. Esta negociação foi encerrada.</p>
          )}

          <div className="mt-3 flex flex-wrap gap-2">
            <Button variant="secondary" onClick={onGoToDashboard}>
              Ir para Dashboard
            </Button>
            <Button onClick={onCreateRequest}>Publicar nova solicitação</Button>
          </div>

          <div className="mt-4 rounded-md border border-emerald-500/20 bg-emerald-500/5 p-3">
            <p className="text-xs font-semibold uppercase tracking-wide text-emerald-200/80">Próximos passos sugeridos</p>
            <ul className="mt-2 list-disc space-y-1 pl-5 text-xs text-emerald-100/80">
              {postAcceptanceChecklist.map((item) => (
                <li key={item.key}>{item.label}</li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
