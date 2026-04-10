import type { SupplierResponseRequest } from '../services/sourcingService';

export type SubmitProposalErrors = Partial<Record<'offerCents' | 'leadTimeDays' | 'warrantyMonths' | 'submit', string>>;

export function validateSubmitProposalForm(formData: SupplierResponseRequest): SubmitProposalErrors {
  const errors: SubmitProposalErrors = {};

  if (formData.offerCents <= 0) {
    errors.offerCents = 'O valor da oferta deve ser maior que zero';
  }

  if (!formData.leadTimeDays || formData.leadTimeDays <= 0) {
    errors.leadTimeDays = 'O prazo de entrega deve ser maior que zero';
  }

  if (formData.warrantyMonths === undefined || formData.warrantyMonths < 0) {
    errors.warrantyMonths = 'A garantia deve ser zero ou positiva';
  }

  if (!formData.supplierId || formData.supplierId.trim().length === 0) {
    errors.submit = 'Não foi possível identificar o vendedor autenticado. Faça login novamente.';
  }

  return errors;
}

export function toNonNegativeInteger(raw: string): number {
  const parsed = Number.parseInt(raw, 10);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0;
}

export function toMoneyCents(raw: string): number {
  const normalized = raw.replace(',', '.');
  const parsed = Number.parseFloat(normalized);
  if (!Number.isFinite(parsed) || parsed <= 0) return 0;
  return Math.round(parsed * 100);
}
