type ProblemDetailsLike = {
  code?: unknown;
  correlationId?: unknown;
  detail?: unknown;
  message?: unknown;
  title?: unknown;
  errors?: unknown;
};

type ProblemCode = 'VALIDATION_ERROR' | 'CONFLICT' | 'UNEXPECTED' | string;

export function extractProblemCode(data: unknown): ProblemCode | null {
  if (!data || typeof data !== 'object') return null;

  const maybeCode = (data as ProblemDetailsLike).code;
  if (typeof maybeCode !== 'string' || maybeCode.trim().length === 0) return null;
  return maybeCode;
}

export function extractProblemMessage(data: unknown): string | null {
  if (!data || typeof data !== 'object') return null;

  const pd = data as ProblemDetailsLike;

  if (typeof pd.detail === 'string' && pd.detail.trim().length > 0) {
    return pd.detail;
  }

  if (typeof pd.message === 'string' && pd.message.trim().length > 0) {
    return pd.message;
  }

  if (typeof pd.title === 'string' && pd.title.trim().length > 0) {
    return pd.title;
  }

  if (pd.errors && typeof pd.errors === 'object') {
    const entries = Object.entries(pd.errors as Record<string, unknown>);
    const first = entries[0];
    if (first && typeof first[1] === 'string' && first[1].trim().length > 0) {
      return first[1];
    }
  }

  return null;
}

export function extractCorrelationId(data: unknown): string | null {
  if (!data || typeof data !== 'object') return null;

  const maybeCorrelationId = (data as ProblemDetailsLike).correlationId;
  if (typeof maybeCorrelationId !== 'string' || maybeCorrelationId.trim().length === 0) {
    return null;
  }

  return maybeCorrelationId;
}

export function appendCorrelationId(message: string, data: unknown): string {
  const correlationId = extractCorrelationId(data);
  if (!correlationId) return message;

  return `${message} (Ref: ${correlationId})`;
}

export function getFriendlyProblemMessage(data: unknown): string | null {
  const message = extractProblemMessage(data);
  if (message) return appendCorrelationId(message, data);

  const code = extractProblemCode(data);
  if (code === 'VALIDATION_ERROR') {
    return appendCorrelationId('Alguns campos estão inválidos. Revise os dados e tente novamente.', data);
  }

  if (code === 'CONFLICT') {
    return appendCorrelationId('Conflito de estado detectado. Atualize a página e tente novamente.', data);
  }

  if (code === 'UNEXPECTED') {
    return appendCorrelationId('Erro inesperado no servidor. Tente novamente em instantes.', data);
  }

  return null;
}

export function getFriendlyHttpErrorMessage(status: number | undefined, data: unknown): string | null {
  if (status === 401) {
    return appendCorrelationId('Sua sessão expirou. Faça login novamente para continuar.', data);
  }

  if (status === 403) {
    return appendCorrelationId('Você não tem permissão para executar esta ação.', data);
  }

  return getFriendlyProblemMessage(data);
}
