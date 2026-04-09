import { z } from 'zod';

// Helper to validate Brazilian phone
const phoneDigits = (val: string) => val.replace(/\D/g, '');

// Login schema
export const loginSchema = z.object({
  identifier: z
    .string()
    .min(1, 'Telefone é obrigatório')
    .refine(
      (val) => {
        const digits = phoneDigits(val);
        return digits.length >= 10 && digits.length <= 11;
      },
      { message: 'Telefone inválido (10 ou 11 dígitos)' }
    ),
  password: z
    .string()
    .min(1, 'Senha é obrigatória')
    .min(8, 'Senha deve ter pelo menos 8 caracteres'),
});

export type LoginFormData = z.infer<typeof loginSchema>;

// Register schema
export const registerSchema = z
  .object({
    name: z.string().min(1, 'Nome é obrigatório').min(3, 'Nome deve ter pelo menos 3 caracteres'),
    phone: z
      .string()
      .min(1, 'Telefone é obrigatório')
      .refine(
        (val) => {
          const digits = phoneDigits(val);
          return digits.length >= 10 && digits.length <= 11;
        },
        { message: 'Telefone inválido (10 ou 11 dígitos)' }
      ),
    documentType: z.enum(['CPF', 'CNPJ']),
    documentNumber: z.string().min(1, 'Documento é obrigatório'),
    password: z
      .string()
      .min(1, 'Senha é obrigatória')
      .min(8, 'Senha deve ter pelo menos 8 caracteres')
      .regex(/[A-Z]/, 'Deve ter pelo menos uma letra maiúscula')
      .regex(/[a-z]/, 'Deve ter pelo menos uma letra minúscula')
      .regex(/[0-9]/, 'Deve ter pelo menos um número')
      .regex(/[^A-Za-z0-9]/, 'Deve ter pelo menos um caractere especial'),
    confirmPassword: z.string().min(1, 'Confirme a senha'),
    role: z.enum(['buyer', 'supplier']),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'As senhas não conferem',
    path: ['confirmPassword'],
  })
  .refine(
    (data) => {
      const digits = phoneDigits(data.documentNumber);
      if (data.documentType === 'CPF') return digits.length === 11;
      if (data.documentType === 'CNPJ') return digits.length === 14;
      return false;
    },
    { message: 'Documento inválido', path: ['documentNumber'] }
  );

export type RegisterFormData = z.infer<typeof registerSchema>;

// Simplified schema for MVP (less strict validation)
export const registerSchemaSimple = z
  .object({
    name: z.string().min(1, 'Nome é obrigatório').min(3, 'Nome deve ter pelo menos 3 caracteres'),
    phone: z
      .string()
      .min(1, 'Telefone é obrigatório')
      .refine(
        (val) => {
          const digits = phoneDigits(val);
          return digits.length >= 10 && digits.length <= 11;
        },
        { message: 'Telefone inválido (10 ou 11 dígitos)' }
      ),
    documentType: z.enum(['CPF', 'CNPJ']),
    documentNumber: z.string().min(1, 'Documento é obrigatório'),
    password: z
      .string()
      .min(1, 'Senha é obrigatória')
      .min(8, 'Senha deve ter pelo menos 8 caracteres'),
    confirmPassword: z.string().min(1, 'Confirme a senha'),
    role: z.enum(['buyer', 'supplier']),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'As senhas não conferem',
    path: ['confirmPassword'],
  })
  .refine(
    (data) => {
      const digits = phoneDigits(data.documentNumber);
      if (data.documentType === 'CPF') return digits.length === 11;
      if (data.documentType === 'CNPJ') return digits.length === 14;
      return false;
    },
    { message: 'Documento inválido', path: ['documentNumber'] }
  );

// CPF/CNPJ validators
export function isValidCpf(value: string): boolean {
  const digits = value.replace(/\D/g, '');
  if (digits.length !== 11 || /^([0-9])\1+$/.test(digits)) return false;

  let sum = 0;
  for (let i = 0; i < 9; i++) sum += Number(digits[i]) * (10 - i);
  let d1 = 11 - (sum % 11);
  if (d1 >= 10) d1 = 0;

  sum = 0;
  for (let i = 0; i < 10; i++) sum += Number(digits[i]) * (11 - i);
  let d2 = 11 - (sum % 11);
  if (d2 >= 10) d2 = 0;

  return digits[9] === String(d1) && digits[10] === String(d2);
}

export function isValidCnpj(value: string): boolean {
  const digits = value.replace(/\D/g, '');
  if (digits.length !== 14 || /^([0-9])\1+$/.test(digits)) return false;

  const weights1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 12; i++) sum += Number(digits[i]) * weights1[i];
  let d1 = 11 - (sum % 11);
  if (d1 >= 10) d1 = 0;

  const weights2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  sum = 0;
  for (let i = 0; i < 13; i++) sum += Number(digits[i]) * weights2[i];
  let d2 = 11 - (sum % 11);
  if (d2 >= 10) d2 = 0;

  return digits[12] === String(d1) && digits[13] === String(d2);
}

// Refine register schema with document validation
export const registerSchemaWithValidation = registerSchema
  .refine(
    (data) => {
      if (data.documentType === 'CPF') return isValidCpf(data.documentNumber);
      return true;
    },
    { message: 'CPF inválido', path: ['documentNumber'] }
  )
  .refine(
    (data) => {
      if (data.documentType === 'CNPJ') return isValidCnpj(data.documentNumber);
      return true;
    },
    { message: 'CNPJ inválido', path: ['documentNumber'] }
  );

export type RegisterFormDataWithValidation = z.infer<typeof registerSchemaWithValidation>;