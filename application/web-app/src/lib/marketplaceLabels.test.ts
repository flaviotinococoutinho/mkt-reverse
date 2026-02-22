import { describe, expect, it } from 'vitest';
import { formatBrlFromCents } from './currency';
import { getEventTypeLabel } from './eventType';
import {
  CONDITION_OPTIONS,
  SHIPPING_MODE_OPTIONS,
  getConditionLabel,
  getShippingModeLabel,
} from './offerTerms';

describe('marketplace display helpers', () => {
  describe('formatBrlFromCents', () => {
    it('formats positive cents to BRL', () => {
      expect(formatBrlFromCents(12345)).toBe('R$ 123,45');
    });

    it('formats zero value', () => {
      expect(formatBrlFromCents(0)).toBe('R$ 0,00');
    });

    it('formats negative cents to BRL', () => {
      expect(formatBrlFromCents(-990)).toBe('-R$ 9,90');
    });
  });

  describe('getEventTypeLabel', () => {
    it('returns known event label', () => {
      expect(getEventTypeLabel('RFQ')).toBe('Cotação (RFQ)');
      expect(getEventTypeLabel('REVERSE_AUCTION')).toBe('Leilão Reverso');
    });

    it('falls back to original type when unknown', () => {
      expect(getEventTypeLabel('BULK_BUY')).toBe('BULK_BUY');
    });
  });

  describe('offer terms labels', () => {
    it('returns translated condition labels for known values', () => {
      expect(getConditionLabel('NEW')).toBe('Novo');
      expect(getConditionLabel('REFURBISHED')).toBe('Recondicionado');
    });

    it('returns translated shipping labels for known values', () => {
      expect(getShippingModeLabel('DELIVERY')).toBe('Entrega Local');
      expect(getShippingModeLabel('DIGITAL')).toBe('Entrega Digital');
    });

    it('returns default when condition/shipping is empty', () => {
      expect(getConditionLabel(undefined)).toBe('Não informado');
      expect(getConditionLabel('')).toBe('Não informado');
      expect(getShippingModeLabel(undefined)).toBe('Não informado');
      expect(getShippingModeLabel('')).toBe('Não informado');
    });

    it('falls back to original value when condition/shipping is unknown', () => {
      expect(getConditionLabel('CERTIFIED_PREOWNED')).toBe('CERTIFIED_PREOWNED');
      expect(getShippingModeLabel('DRONE')).toBe('DRONE');
    });

    it('exposes expected option values for UI select controls', () => {
      expect(CONDITION_OPTIONS.map((option) => option.value)).toEqual([
        'NEW',
        'USED',
        'REFURBISHED',
        'FOR_PARTS',
        'UNKNOWN',
      ]);

      expect(SHIPPING_MODE_OPTIONS.map((option) => option.value)).toEqual([
        'PICKUP',
        'DELIVERY',
        'SHIPPING',
        'DIGITAL',
        'UNKNOWN',
      ]);
    });
  });
});
