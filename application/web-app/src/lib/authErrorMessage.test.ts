import { describe, expect, it } from 'vitest';
import { getFriendlyAuthErrorMessage } from './authErrorMessage';

describe('getFriendlyAuthErrorMessage', () => {
  it('returns specific login invalid-credentials message for 400/401', () => {
    expect(getFriendlyAuthErrorMessage('login', 400, {})).toBe('Telefone/WhatsApp ou senha inválidos.');
    expect(getFriendlyAuthErrorMessage('login', 401, {})).toBe('Telefone/WhatsApp ou senha inválidos.');
  });

  it('returns specific register duplicate-account message for conflict', () => {
    expect(getFriendlyAuthErrorMessage('register', 409, {})).toBe(
      'Já existe uma conta com este telefone/documento.',
    );
    expect(getFriendlyAuthErrorMessage('register', 500, { code: 'CONFLICT' })).toBe(
      'Já existe uma conta com este telefone/documento.',
    );
    expect(getFriendlyAuthErrorMessage('register', 500, { detail: 'User already exists' })).toBe(
      'Já existe uma conta com este telefone/documento.',
    );
  });

  it('returns specific register validation message for 400/422 and validation code', () => {
    expect(getFriendlyAuthErrorMessage('register', 400, {})).toBe(
      'Não foi possível concluir o cadastro. Revise os dados e tente novamente.',
    );
    expect(getFriendlyAuthErrorMessage('register', 422, {})).toBe(
      'Não foi possível concluir o cadastro. Revise os dados e tente novamente.',
    );
    expect(getFriendlyAuthErrorMessage('register', 500, { code: 'VALIDATION_ERROR' })).toBe(
      'Não foi possível concluir o cadastro. Revise os dados e tente novamente.',
    );
  });

  it('falls back to generic Problem Details mapping when no auth-specific rule matches', () => {
    expect(getFriendlyAuthErrorMessage('login', 403, {})).toBe(
      'Você não tem permissão para executar esta ação.',
    );
  });

  it('appends correlation id in auth-specific messages', () => {
    expect(getFriendlyAuthErrorMessage('register', 409, { correlationId: 'corr-88' })).toBe(
      'Já existe uma conta com este telefone/documento. (Ref: corr-88)',
    );
  });
});
