import { describe, expect, it } from 'vitest';
import { buildCreateRequestErrorToast } from './createRequestSubmit';

describe('buildCreateRequestErrorToast', () => {
  it('inclui mensagem amigável e correlationId quando backend retorna ProblemDetails', () => {
    const toast = buildCreateRequestErrorToast({
      response: {
        status: 409,
        data: {
          code: 'CONFLICT',
          correlationId: 'corr-123',
        },
      },
    });

    expect(toast.title).toBe('Erro ao criar solicitação');
    expect(toast.description).toBe('Conflito de estado detectado. Atualize a página e tente novamente. (Ref: corr-123)');
  });

  it('aplica fallback padrão quando erro não possui metadados HTTP', () => {
    const toast = buildCreateRequestErrorToast(new Error('boom'));

    expect(toast).toEqual({
      title: 'Erro ao criar solicitação',
      description: 'Revise os campos e tente novamente.',
    });
  });
});
