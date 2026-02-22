import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { UserPlus } from 'lucide-react';
import { authService } from '../../services/authService';
import { useAuth } from '../../context/useAuth';
import axios from 'axios';
import { formatBrazilPhone } from '../../lib/phone';
import { resetOnboardingState } from '../../lib/onboarding';
import { getFriendlyAuthErrorMessage } from '../../lib/authErrorMessage';
import { isStrongPassword, isValidBrazilPhone, isValidCnpj, isValidCpf } from '../../lib/authValidation';

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

function generateTestCpf(): string {
  while (true) {
    const base9 = Array.from({ length: 9 }, () => Math.floor(Math.random() * 10));
    if (base9.every((d) => d === base9[0])) continue;
    const [d1, d2] = computeCpfCheckDigits(base9);
    return [...base9, d1, d2].join('');
  }
}

function computeCnpjCheckDigits(base12: number[]): [number, number] {
  const weights1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  let sum1 = 0;
  for (let i = 0; i < 12; i++) sum1 += base12[i] * weights1[i];
  let d1 = 11 - (sum1 % 11);
  if (d1 >= 10) d1 = 0;

  const weights2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  const base13 = [...base12, d1];
  let sum2 = 0;
  for (let i = 0; i < 13; i++) sum2 += base13[i] * weights2[i];
  let d2 = 11 - (sum2 % 11);
  if (d2 >= 10) d2 = 0;

  return [d1, d2];
}

function generateTestCnpj(): string {
  while (true) {
    const base12 = Array.from({ length: 12 }, () => Math.floor(Math.random() * 10));
    if (base12.every((d) => d === base12[0])) continue;
    const [d1, d2] = computeCnpjCheckDigits(base12);
    return [...base12, d1, d2].join('');
  }
}

export default function Register() {
  const [role, setRole] = useState<'buyer' | 'supplier'>('buyer');
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    documentNumber: '',
    documentType: 'CPF',
    password: '',
    confirmPassword: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    if (name === 'phone') {
      setFormData({ ...formData, phone: formatBrazilPhone(value) });
      return;
    }
    setFormData({ ...formData, [name]: value });
  };

  const setDocumentType = (documentType: 'CPF' | 'CNPJ') => {
    setFormData({ ...formData, documentType });
  };

  const phoneError =
    formData.phone.length > 0 && !isValidBrazilPhone(formData.phone)
      ? 'Informe um telefone/WhatsApp válido (10 ou 11 dígitos).'
      : null;

  const documentError = (() => {
    if (!formData.documentNumber) return null;
    if (formData.documentType === 'CPF' && !isValidCpf(formData.documentNumber)) {
      return 'CPF inválido.';
    }
    if (formData.documentType === 'CNPJ' && !isValidCnpj(formData.documentNumber)) {
      return 'CNPJ inválido.';
    }
    return null;
  })();

  const passwordError =
    formData.password.length > 0 && !isStrongPassword(formData.password)
      ? 'Use 8+ caracteres com maiúscula, minúscula, número e especial.'
      : null;

  const confirmPasswordError =
    formData.confirmPassword.length > 0 && formData.password !== formData.confirmPassword
      ? 'As senhas não conferem.'
      : null;

  const isFormInvalid =
    !formData.name.trim()
    || !formData.phone
    || !formData.documentNumber
    || !formData.password
    || !formData.confirmPassword
    || !!phoneError
    || !!documentError
    || !!passwordError
    || !!confirmPasswordError;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (isFormInvalid) {
      setErrorMessage('Revise os campos obrigatórios antes de cadastrar.');
      return;
    }
    
    setIsLoading(true);
    try {
      resetOnboardingState();
      const response = await authService.register({
        name: formData.name,
        phone: formData.phone,
        password: formData.password,
        role: role,
        documentNumber: formData.documentNumber,
        documentType: formData.documentType as 'CPF' | 'CNPJ',
      });
      login(response.user, response.token);
      
      // Onboarding step: phone verification UI (simulated in MVP)
      navigate('/verify-phone');
    } catch (error) {
      console.error('Registration failed', error);

      if (axios.isAxiosError(error)) {
        const msg = getFriendlyAuthErrorMessage('register', error.response?.status, error.response?.data);
        setErrorMessage(msg || error.message);
        return;
      }

      setErrorMessage('Falha ao cadastrar. Tente novamente.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-ink p-4 text-zinc-200 font-sans">
      <div className="auction-panel w-full max-w-sm space-y-8 bg-ink/50 p-8 shadow-xl">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-serif text-citrus">Criar Conta</h1>
          <p className="text-zinc-400 text-sm">Cadastre-se para participar</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {errorMessage ? (
            <div className="rounded-lg border border-danger/40 bg-danger/10 px-3 py-2 text-sm text-zinc-100">
              <div className="font-mono text-xs text-danger">CADASTRO_FALHOU</div>
              <div className="mt-1">{errorMessage}</div>
              <div className="mt-2 text-xs text-zinc-400">
                Dica: CPF/CNPJ precisa ser <span className="text-zinc-200">válido (dígitos verificadores)</span>.
              </div>
            </div>
          ) : null}

          <div className="flex justify-center space-x-4 pb-2">
            <button
              type="button"
              onClick={() => setRole('buyer')}
              className={`px-4 py-2 rounded-md transition-colors ${role === 'buyer' ? 'bg-citrus text-ink font-bold' : 'bg-ink border border-stroke text-zinc-400'}`}
            >
              Comprador
            </button>
            <button
              type="button"
              onClick={() => setRole('supplier')}
              className={`px-4 py-2 rounded-md transition-colors ${role === 'supplier' ? 'bg-citrus text-ink font-bold' : 'bg-ink border border-stroke text-zinc-400'}`}
            >
              Vendedor
            </button>
          </div>

          <Input
            name="name"
            label="Nome Completo / Empresa"
            placeholder="Seu nome"
            value={formData.name}
            onChange={handleChange}
            required
          />

          <Input
            name="phone"
            type="tel"
            label="Telefone / WhatsApp"
            placeholder="(11) 99999-9999"
            value={formData.phone}
            onChange={handleChange}
            required
            error={phoneError ?? undefined}
          />

          <p className="text-xs text-zinc-500 leading-relaxed -mt-4">
            No MVP, o acesso é centrado em <span className="text-zinc-300">telefone/WhatsApp</span>.
            Não há fluxo de email para cadastro, login ou notificações.
          </p>

          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="text-sm text-zinc-400">Documento</label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setDocumentType('CPF')}
                  className={`px-3 py-1 rounded-md text-xs transition-colors ${formData.documentType === 'CPF' ? 'bg-citrus text-ink font-bold' : 'bg-ink border border-stroke text-zinc-400'}`}
                >
                  CPF
                </button>
                <button
                  type="button"
                  onClick={() => setDocumentType('CNPJ')}
                  className={`px-3 py-1 rounded-md text-xs transition-colors ${formData.documentType === 'CNPJ' ? 'bg-citrus text-ink font-bold' : 'bg-ink border border-stroke text-zinc-400'}`}
                >
                  CNPJ
                </button>
              </div>
            </div>

            <Input
              name="documentNumber"
              label={formData.documentType}
              placeholder={formData.documentType === 'CPF' ? 'CPF válido (11 dígitos)' : 'CNPJ válido (14 dígitos)'}
              value={formData.documentNumber}
              onChange={handleChange}
              required
              error={documentError ?? undefined}
            />

            <div className="flex items-center justify-between text-xs text-zinc-500">
              <span>
                Para demo local, você pode usar um documento real ou gerar um de teste.
              </span>
              <button
                type="button"
                className="font-mono text-citrus hover:underline"
                onClick={() =>
                  setFormData({
                    ...formData,
                    documentNumber:
                      formData.documentType === 'CPF' ? generateTestCpf() : generateTestCnpj(),
                  })
                }
              >
                gerar
              </button>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
             <Input
              name="password"
              type="password"
              label="Senha"
              placeholder="•••••••"
              value={formData.password}
              onChange={handleChange}
              required
              error={passwordError ?? undefined}
            />
             <Input
              name="confirmPassword"
              type="password"
              label="Confirmar"
              placeholder="•••••••"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              error={confirmPasswordError ?? undefined}
            />
          </div>

          <p className="text-xs text-zinc-500 leading-relaxed">
            Regras da senha (MVP): mínimo 8 caracteres, com <span className="text-zinc-300">maiúscula</span>, <span className="text-zinc-300">minúscula</span>, <span className="text-zinc-300">número</span> e <span className="text-zinc-300">especial</span> (ex: <span className="font-mono">@</span>).
          </p>

          <Button
            type="submit"
            className="w-full"
            isLoading={isLoading}
            size="lg"
            disabled={isFormInvalid}
          >
            <UserPlus className="mr-2 h-4 w-4" />
            Cadastrar
          </Button>
        </form>

        <div className="text-center text-sm text-zinc-500">
          Já tem uma conta?{' '}
          <Link to="/login" className="text-citrus hover:underline font-medium">
            Entrar
          </Link>
        </div>
      </div>
    </div>
  );
}
