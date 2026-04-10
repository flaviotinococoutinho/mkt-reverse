import { digitsOnly } from './phone';

function computeCpfCheckDigits(base9: number[]): [number, number] {
  let sum1 = 0;
  for (let i = 0; i < 9; i += 1) sum1 += base9[i] * (10 - i);
  let d1 = 11 - (sum1 % 11);
  if (d1 >= 10) d1 = 0;

  const base10 = [...base9, d1];
  let sum2 = 0;
  for (let i = 0; i < 10; i += 1) sum2 += base10[i] * (11 - i);
  let d2 = 11 - (sum2 % 11);
  if (d2 >= 10) d2 = 0;

  return [d1, d2];
}

function computeCnpjCheckDigits(base12: number[]): [number, number] {
  const weights1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  let sum1 = 0;
  for (let i = 0; i < 12; i += 1) sum1 += base12[i] * weights1[i];
  let d1 = 11 - (sum1 % 11);
  if (d1 >= 10) d1 = 0;

  const weights2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  const base13 = [...base12, d1];
  let sum2 = 0;
  for (let i = 0; i < 13; i += 1) sum2 += base13[i] * weights2[i];
  let d2 = 11 - (sum2 % 11);
  if (d2 >= 10) d2 = 0;

  return [d1, d2];
}

export function isValidBrazilPhone(value: string): boolean {
  const digits = digitsOnly(value);
  return digits.length >= 10 && digits.length <= 11;
}

export function isValidCpf(value: string): boolean {
  const digits = digitsOnly(value);
  if (digits.length !== 11 || /^([0-9])\1+$/.test(digits)) return false;
  const numbers = digits.split('').map(Number);
  const [d1, d2] = computeCpfCheckDigits(numbers.slice(0, 9));
  return numbers[9] === d1 && numbers[10] === d2;
}

export function isValidCnpj(value: string): boolean {
  const digits = digitsOnly(value);
  if (digits.length !== 14 || /^([0-9])\1+$/.test(digits)) return false;
  const numbers = digits.split('').map(Number);
  const [d1, d2] = computeCnpjCheckDigits(numbers.slice(0, 12));
  return numbers[12] === d1 && numbers[13] === d2;
}

export function isStrongPassword(value: string): boolean {
  const passwordRule = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/;
  return passwordRule.test(value);
}
