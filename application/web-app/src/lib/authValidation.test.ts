import { describe, expect, it } from 'vitest';
import {
  isStrongPassword,
  isValidBrazilPhone,
  isValidCnpj,
  isValidCpf,
} from './authValidation';

describe('authValidation', () => {
  it('validates brazil phone (10-11 digits)', () => {
    expect(isValidBrazilPhone('(11) 3333-4444')).toBe(true);
    expect(isValidBrazilPhone('(11) 99999-0000')).toBe(true);
    expect(isValidBrazilPhone('11999990000')).toBe(true);

    expect(isValidBrazilPhone('119999')).toBe(false);
    expect(isValidBrazilPhone('5511999990000123')).toBe(false);
  });

  it('validates CPF check digits', () => {
    expect(isValidCpf('529.982.247-25')).toBe(true);
    expect(isValidCpf('111.111.111-11')).toBe(false);
    expect(isValidCpf('529.982.247-24')).toBe(false);
  });

  it('validates CNPJ check digits', () => {
    expect(isValidCnpj('04.252.011/0001-10')).toBe(true);
    expect(isValidCnpj('11.111.111/1111-11')).toBe(false);
    expect(isValidCnpj('04.252.011/0001-11')).toBe(false);
  });

  it('enforces strong password rule', () => {
    expect(isStrongPassword('Abcd1234@')).toBe(true);
    expect(isStrongPassword('abcd1234@')).toBe(false);
    expect(isStrongPassword('ABCD1234@')).toBe(false);
    expect(isStrongPassword('Abcdefgh@')).toBe(false);
    expect(isStrongPassword('Abcd1234')).toBe(false);
  });
});
