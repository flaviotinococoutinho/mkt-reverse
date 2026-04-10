export function digitsOnly(value: string): string {
  return value.replace(/\D/g, '');
}

export function formatBrazilPhone(value: string): string {
  const digits = digitsOnly(value);
  if (digits.length <= 2) return digits;
  if (digits.length <= 6) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
  if (digits.length <= 10) return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
  return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7, 11)}`;
}

export function phoneToMvpEmail(phone: string): string {
  const digits = digitsOnly(phone);
  return `${digits}@queroja.mvp`;
}
