import { getFriendlyHttpErrorMessage } from './problemDetails';

type HttpErrorLike = {
  response?: {
    status?: number;
    data?: unknown;
  };
};

export type CreateRequestErrorToast = {
  title: string;
  description: string;
};

export function buildCreateRequestErrorToast(error: unknown): CreateRequestErrorToast {
  const maybeHttpError = error as HttpErrorLike;
  const message = getFriendlyHttpErrorMessage(
    maybeHttpError?.response?.status,
    maybeHttpError?.response?.data,
  );

  return {
    title: 'Erro ao criar solicitação',
    description: message ?? 'Revise os campos e tente novamente.',
  };
}
