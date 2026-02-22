const STATUS_LABELS: Record<string, string> = {
  PUBLISHED: 'Publicado',
  IN_PROGRESS: 'Em negociação',
  SUBMITTED: 'Enviada',
  ACCEPTED: 'Aceita',
  REJECTED: 'Rejeitada',
  WITHDRAWN: 'Retirada',
  AWARDED: 'Vencedora',
  CLOSED: 'Encerrado',
  CANCELLED: 'Cancelado',
};

export function getStatusLabel(status: string): string {
  return STATUS_LABELS[status] ?? status;
}
