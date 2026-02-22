import { describe, expect, it } from 'vitest';
import {
  canSubmitProposalForEventStatus,
  getProposalSubmissionBlockReason,
} from './proposalSubmissionAvailability';

describe('proposalSubmissionAvailability', () => {
  it('permite envio quando status está aberto para propostas', () => {
    expect(canSubmitProposalForEventStatus('PUBLISHED')).toBe(true);
    expect(canSubmitProposalForEventStatus('IN_PROGRESS')).toBe(true);
  });

  it('bloqueia envio quando status não aceita novas propostas', () => {
    expect(canSubmitProposalForEventStatus('AWARDED')).toBe(false);
    expect(canSubmitProposalForEventStatus('CLOSED')).toBe(false);
  });

  it('retorna motivo amigável quando bloqueado', () => {
    expect(getProposalSubmissionBlockReason('AWARDED')).toBe(
      'Esta solicitação não está mais aberta para novas propostas.',
    );
    expect(getProposalSubmissionBlockReason(undefined)).toBe(
      'Não foi possível validar o status da solicitação.',
    );
    expect(getProposalSubmissionBlockReason('PUBLISHED')).toBeNull();
  });
});
