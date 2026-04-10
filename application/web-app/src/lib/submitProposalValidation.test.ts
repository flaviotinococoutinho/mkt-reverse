import { describe, expect, it } from 'vitest';
import {
  toMoneyCents,
  toNonNegativeInteger,
  validateSubmitProposalForm,
} from './submitProposalValidation';
import type { SupplierResponseRequest } from '../services/sourcingService';

function makeValidProposal(overrides: Partial<SupplierResponseRequest> = {}): SupplierResponseRequest {
  return {
    supplierId: 'supplier-123',
    offerCents: 125000,
    leadTimeDays: 7,
    warrantyMonths: 12,
    condition: 'NEW',
    shippingMode: 'SHIPPING',
    message: 'Entrega com nota fiscal e garantia.',
    attributes: [],
    ...overrides,
  };
}

describe('validateSubmitProposalForm', () => {
  it('retorna sem erros para proposta válida', () => {
    const result = validateSubmitProposalForm(makeValidProposal());

    expect(result).toEqual({});
  });

  it('invalida oferta menor ou igual a zero', () => {
    const result = validateSubmitProposalForm(makeValidProposal({ offerCents: 0 }));

    expect(result.offerCents).toContain('maior que zero');
  });

  it('invalida prazo menor ou igual a zero', () => {
    const result = validateSubmitProposalForm(makeValidProposal({ leadTimeDays: 0 }));

    expect(result.leadTimeDays).toContain('maior que zero');
  });

  it('invalida garantia negativa', () => {
    const result = validateSubmitProposalForm(makeValidProposal({ warrantyMonths: -1 }));

    expect(result.warrantyMonths).toContain('zero ou positiva');
  });

  it('invalida quando supplierId está ausente', () => {
    const result = validateSubmitProposalForm(makeValidProposal({ supplierId: '  ' }));

    expect(result.submit).toContain('vendedor autenticado');
  });
});

describe('submitProposal value parsers', () => {
  it('converte moeda BRL para centavos com vírgula e ponto', () => {
    expect(toMoneyCents('123.45')).toBe(12345);
    expect(toMoneyCents('99,90')).toBe(9990);
  });

  it('retorna zero para moeda inválida ou não positiva', () => {
    expect(toMoneyCents('')).toBe(0);
    expect(toMoneyCents('-10')).toBe(0);
    expect(toMoneyCents('abc')).toBe(0);
  });

  it('converte inteiros não negativos para prazo/garantia', () => {
    expect(toNonNegativeInteger('15')).toBe(15);
    expect(toNonNegativeInteger('0')).toBe(0);
    expect(toNonNegativeInteger('-1')).toBe(0);
    expect(toNonNegativeInteger('abc')).toBe(0);
  });
});
