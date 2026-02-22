import { describe, expect, it } from 'vitest';
import {
  appendCorrelationId,
  extractCorrelationId,
  extractProblemCode,
  extractProblemMessage,
  getFriendlyHttpErrorMessage,
  getFriendlyProblemMessage,
} from './problemDetails';

describe('extractProblemCode', () => {
  it('returns code when it is a non-empty string', () => {
    expect(extractProblemCode({ code: 'VALIDATION_ERROR' })).toBe('VALIDATION_ERROR');
  });

  it('returns null when code is missing, blank, or not a string', () => {
    expect(extractProblemCode({})).toBeNull();
    expect(extractProblemCode({ code: '   ' })).toBeNull();
    expect(extractProblemCode({ code: 42 })).toBeNull();
  });

  it('returns null for non-object payloads', () => {
    expect(extractProblemCode(null)).toBeNull();
    expect(extractProblemCode('oops')).toBeNull();
  });
});

describe('extractProblemMessage', () => {
  it('prioritizes detail over message/title/errors', () => {
    expect(
      extractProblemMessage({
        detail: 'detail text',
        message: 'message text',
        title: 'title text',
        errors: { field: 'field error' },
      }),
    ).toBe('detail text');
  });

  it('falls back from message to title', () => {
    expect(extractProblemMessage({ message: 'message text' })).toBe('message text');
    expect(extractProblemMessage({ title: 'title text' })).toBe('title text');
  });

  it('extracts first errors[] message when available', () => {
    expect(extractProblemMessage({ errors: { email: 'Email inválido' } })).toBe('Email inválido');
  });

  it('returns null for empty or unsupported values', () => {
    expect(extractProblemMessage({ detail: '   ' })).toBeNull();
    expect(extractProblemMessage({ errors: { email: ['not-string'] } })).toBeNull();
    expect(extractProblemMessage(null)).toBeNull();
  });
});

describe('getFriendlyProblemMessage', () => {
  it('returns extracted message when present', () => {
    expect(getFriendlyProblemMessage({ detail: 'Mensagem do backend' })).toBe('Mensagem do backend');
  });

  it('falls back by known codes', () => {
    expect(getFriendlyProblemMessage({ code: 'VALIDATION_ERROR' })).toBe(
      'Alguns campos estão inválidos. Revise os dados e tente novamente.',
    );
    expect(getFriendlyProblemMessage({ code: 'CONFLICT' })).toBe(
      'Conflito de estado detectado. Atualize a página e tente novamente.',
    );
    expect(getFriendlyProblemMessage({ code: 'UNEXPECTED' })).toBe(
      'Erro inesperado no servidor. Tente novamente em instantes.',
    );
  });

  it('returns null for unknown code and no message', () => {
    expect(getFriendlyProblemMessage({ code: 'SOMETHING_ELSE' })).toBeNull();
  });

  it('appends correlation id when available', () => {
    expect(
      getFriendlyProblemMessage({
        code: 'UNEXPECTED',
        correlationId: 'corr-123',
      }),
    ).toBe('Erro inesperado no servidor. Tente novamente em instantes. (Ref: corr-123)');
  });
});

describe('extractCorrelationId', () => {
  it('returns a non-empty correlation id string', () => {
    expect(extractCorrelationId({ correlationId: 'abc-123' })).toBe('abc-123');
  });

  it('returns null when payload has no correlation id', () => {
    expect(extractCorrelationId({})).toBeNull();
    expect(extractCorrelationId({ correlationId: '' })).toBeNull();
    expect(extractCorrelationId({ correlationId: 123 })).toBeNull();
    expect(extractCorrelationId(null)).toBeNull();
  });
});

describe('appendCorrelationId', () => {
  it('appends reference when correlation id exists', () => {
    expect(appendCorrelationId('Erro', { correlationId: 'corr-99' })).toBe('Erro (Ref: corr-99)');
  });

  it('returns original message when correlation id is missing', () => {
    expect(appendCorrelationId('Erro', {})).toBe('Erro');
  });
});

describe('getFriendlyHttpErrorMessage', () => {
  it('maps 401 to session-expired message', () => {
    expect(getFriendlyHttpErrorMessage(401, {})).toBe('Sua sessão expirou. Faça login novamente para continuar.');
  });

  it('preserva correlationId no fallback de 401', () => {
    expect(getFriendlyHttpErrorMessage(401, { correlationId: 'corr-401' })).toBe(
      'Sua sessão expirou. Faça login novamente para continuar. (Ref: corr-401)',
    );
  });

  it('maps 403 to forbidden message', () => {
    expect(getFriendlyHttpErrorMessage(403, {})).toBe('Você não tem permissão para executar esta ação.');
  });

  it('preserva correlationId no fallback de 403', () => {
    expect(getFriendlyHttpErrorMessage(403, { correlationId: 'corr-403' })).toBe(
      'Você não tem permissão para executar esta ação. (Ref: corr-403)',
    );
  });

  it('falls back to ProblemDetails parsing for other statuses', () => {
    expect(getFriendlyHttpErrorMessage(500, { code: 'UNEXPECTED' })).toBe(
      'Erro inesperado no servidor. Tente novamente em instantes.',
    );
  });
});
