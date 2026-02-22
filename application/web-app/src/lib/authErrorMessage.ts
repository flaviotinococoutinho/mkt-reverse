import { appendCorrelationId, extractProblemCode, extractProblemMessage, getFriendlyHttpErrorMessage } from './problemDetails';

export type AuthFlow = 'login' | 'register';

function looksLikeDuplicateIdentity(detail: string | null): boolean {
  if (!detail) return false;
  const normalized = detail.toLowerCase();

  return (
    normalized.includes('already')
    || normalized.includes('exists')
    || normalized.includes('duplic')
    || normalized.includes('já cadastrad')
    || normalized.includes('já existe')
    || normalized.includes('conflit')
  );
}

export function getFriendlyAuthErrorMessage(
  flow: AuthFlow,
  status: number | undefined,
  data: unknown,
): string {
  if (flow === 'login') {
    if (status === 400 || status === 401) {
      return appendCorrelationId('Telefone/WhatsApp ou senha inválidos.', data);
    }
  }

  if (flow === 'register') {
    const code = extractProblemCode(data);
    const detail = extractProblemMessage(data);

    if (status === 409 || code === 'CONFLICT' || looksLikeDuplicateIdentity(detail)) {
      return appendCorrelationId('Já existe uma conta com este telefone/documento.', data);
    }

    if (status === 400 || status === 422 || code === 'VALIDATION_ERROR') {
      return appendCorrelationId('Não foi possível concluir o cadastro. Revise os dados e tente novamente.', data);
    }
  }

  return (
    getFriendlyHttpErrorMessage(status, data)
    ?? appendCorrelationId('Não foi possível completar a autenticação agora. Tente novamente.', data)
  );
}
