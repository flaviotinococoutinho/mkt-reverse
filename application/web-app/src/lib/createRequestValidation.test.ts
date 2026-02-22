import { describe, expect, it } from 'vitest';
import { validateCreateRequestForm } from './createRequestValidation';
import type { CreateSourcingEventRequest } from '../services/sourcingService';

function makeValidForm(overrides: Partial<CreateSourcingEventRequest> = {}): CreateSourcingEventRequest {
  return {
    tenantId: 'tenant-default',
    buyerOrganizationId: 'org-1',
    buyerContactName: 'Flavio',
    buyerContactPhone: '(11) 99999-9999',
    title: 'Compra de notebooks',
    description: 'Solicitação para equipe comercial',
    type: 'RFQ',
    productName: 'Notebook Dell Latitude 7490',
    productDescription: 'Notebook i7, 16GB RAM, SSD 512GB, garantia de 12 meses',
    category: 'Informática',
    unitOfMeasure: 'un',
    quantityRequired: 10,
    validForHours: 168,
    ...overrides,
  };
}

describe('validateCreateRequestForm', () => {
  it('retorna formulário válido quando todos os campos críticos estão corretos', () => {
    const result = validateCreateRequestForm(makeValidForm());

    expect(result.contactPhoneError).toBeNull();
    expect(result.titleError).toBeNull();
    expect(result.descriptionError).toBeNull();
    expect(result.quantityError).toBeNull();
    expect(result.isInvalidForPreview).toBe(false);
  });

  it('invalida telefone com menos de 10 dígitos', () => {
    const result = validateCreateRequestForm(makeValidForm({ buyerContactPhone: '(11) 9999-999' }));

    expect(result.contactPhoneError).toContain('10 ou 11 dígitos');
    expect(result.isInvalidForPreview).toBe(true);
  });

  it('invalida título com menos de 8 caracteres', () => {
    const result = validateCreateRequestForm(makeValidForm({ title: 'Curto' }));

    expect(result.titleError).toContain('pelo menos 8 caracteres');
    expect(result.isInvalidForPreview).toBe(true);
  });

  it('invalida descrição detalhada curta quando preenchida', () => {
    const result = validateCreateRequestForm(makeValidForm({ productDescription: 'Pouco detalhe' }));

    expect(result.descriptionError).toContain('mínimo de 20 caracteres');
    expect(result.isInvalidForPreview).toBe(true);
  });

  it('não invalida quando descrição detalhada está vazia', () => {
    const result = validateCreateRequestForm(makeValidForm({ productDescription: '' }));

    expect(result.descriptionError).toBeNull();
    expect(result.isInvalidForPreview).toBe(false);
  });

  it('invalida quantidade não inteira ou menor que 1', () => {
    const nonInteger = validateCreateRequestForm(makeValidForm({ quantityRequired: 1.5 }));
    const zero = validateCreateRequestForm(makeValidForm({ quantityRequired: 0 }));

    expect(nonInteger.quantityError).toContain('número inteiro maior que zero');
    expect(nonInteger.isInvalidForPreview).toBe(true);
    expect(zero.quantityError).toContain('número inteiro maior que zero');
    expect(zero.isInvalidForPreview).toBe(true);
  });

  it('invalida preview quando falta um campo obrigatório mesmo sem erro específico', () => {
    const result = validateCreateRequestForm(makeValidForm({ productName: '   ' }));

    expect(result.contactPhoneError).toBeNull();
    expect(result.titleError).toBeNull();
    expect(result.isInvalidForPreview).toBe(true);
  });
});
