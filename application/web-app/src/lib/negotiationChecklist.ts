export type NegotiationChecklistItem = {
  key: string;
  label: string;
};

export function getPostAcceptanceChecklist(eventStatus?: string): NegotiationChecklistItem[] {
  const baseItems: NegotiationChecklistItem[] = [
    {
      key: 'contact',
      label: 'Confirmar canal de contato com o vendedor (WhatsApp/telefone).',
    },
    {
      key: 'timeline',
      label: 'Alinhar prazo de entrega e marcos principais.',
    },
    {
      key: 'evidence',
      label: 'Registrar termos finais acordados para referência.',
    },
  ];

  if (eventStatus === 'AWARDED' || eventStatus === 'CLOSED') {
    return [
      ...baseItems,
      {
        key: 'closure',
        label: 'Solicitação encerrada: iniciar nova solicitação se ainda houver demanda.',
      },
    ];
  }

  return baseItems;
}

