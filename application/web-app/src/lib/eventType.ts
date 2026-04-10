const EVENT_TYPE_LABELS: Record<string, string> = {
  RFQ: 'Cotação (RFQ)',
  REVERSE_AUCTION: 'Leilão Reverso',
};

export function getEventTypeLabel(type: string): string {
  return EVENT_TYPE_LABELS[type] ?? type;
}

