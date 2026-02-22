import type { CreateSourcingEventRequest } from '../services/sourcingService';
import { digitsOnly } from './phone';

export interface CreateRequestValidationResult {
  contactPhoneError: string | null;
  titleError: string | null;
  descriptionError: string | null;
  quantityError: string | null;
  isInvalidForPreview: boolean;
}

export function validateCreateRequestForm(
  formData: CreateSourcingEventRequest,
): CreateRequestValidationResult {
  const phoneDigits = digitsOnly(formData.buyerContactPhone || '');
  const contactPhoneError =
    formData.buyerContactPhone.length > 0 && (phoneDigits.length < 10 || phoneDigits.length > 11)
      ? 'Informe um telefone/WhatsApp válido (10 ou 11 dígitos).'
      : null;

  const titleError =
    formData.title.trim().length > 0 && formData.title.trim().length < 8
      ? 'Use pelo menos 8 caracteres para um título claro.'
      : null;

  const descriptionError =
    formData.productDescription
    && formData.productDescription.trim().length > 0
    && formData.productDescription.trim().length < 20
      ? 'Adicione mais detalhes (mínimo de 20 caracteres).'
      : null;

  const quantityError =
    !Number.isInteger(formData.quantityRequired) || formData.quantityRequired < 1
      ? 'Quantidade deve ser um número inteiro maior que zero.'
      : null;

  const isInvalidForPreview =
    !formData.productName.trim()
    || !formData.title.trim()
    || !formData.buyerContactName.trim()
    || !formData.buyerContactPhone.trim()
    || !formData.unitOfMeasure.trim()
    || !!contactPhoneError
    || !!titleError
    || !!descriptionError
    || !!quantityError;

  return {
    contactPhoneError,
    titleError,
    descriptionError,
    quantityError,
    isInvalidForPreview,
  };
}
