const ALLOWED_EVENT_STATUSES = new Set(['PUBLISHED', 'IN_PROGRESS']);

export function canSubmitProposalForEventStatus(status: string | null | undefined): boolean {
  if (!status) return false;
  return ALLOWED_EVENT_STATUSES.has(status);
}

export function getProposalSubmissionBlockReason(status: string | null | undefined): string | null {
  if (!status) {
    return 'Não foi possível validar o status da solicitação.';
  }

  if (canSubmitProposalForEventStatus(status)) {
    return null;
  }

  return 'Esta solicitação não está mais aberta para novas propostas.';
}
