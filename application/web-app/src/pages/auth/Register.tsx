import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '../../components/ui/Button';
import { UserPlus } from 'lucide-react';
import { authService } from '../../services/authService';
import { useAuth } from '../../context/useAuth';
import axios from 'axios';
import { formatBrazilPhone } from '../../lib/phone';
import { resetOnboardingState } from '../../lib/onboarding';
import { getFriendlyAuthErrorMessage } from '../../lib/authErrorMessage';
import { registerSchemaSimple, RegisterFormData } from '../../lib/schemas/auth';
import { useToast } from '../../components/ui/feedback';

function generateTestCpf(): string {
  const base9 = Array.from({ length: 9 }, () => Math.floor(Math.random() * 10));
  if (base9.every((d) => d === base9[0])) return generateTestCpf();
  
  let sum1 = 0;
  for (let i = 0; i < 9; i++) sum1 += base9[i] * (10 - i);
  let d1 = 11 - (sum1 % 11);
  if (d1 >= 10) d1 = 0;

  const base10 = [...base9, d1];
  let sum2 = 0;
  for (let i = 0; i < 10; i++) sum2 += base10[i] * (11 - i);
  let d2 = 11 - (sum2 % 11);
  if (d2 >= 10) d2 = 0;

  return [...base9, d1, d2].join('');
}

function generateTestCnpj(): string {
  const base12 = Array.from({ length: 12 }, () => Math.floor(Math.random() * 10));
  if (base12.every((d) => d === base12[0])) return generateTestCnpj();

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

  return [...base12, d1, d2].join('');
}

export default function Register() {
  const [serverError, setServerError] = React.useState<string | null>(null);
  const navigate = useNavigate();
  const { login } = useAuth();
  const { error: showError, success } = useToast();

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchemaSimple),
    defaultValues: {
      name: '',
      phone: '',
      documentNumber: '',
      documentType: 'CPF',
      password: '',
      confirmPassword: '',
      role: 'buyer',
    },
  });

  const documentType = watch('documentType');

  const onSubmit = async (data: RegisterFormData) => {
    setServerError(null);
    
    try {
      resetOnboardingState();
      const response = await authService.register({
        name: data.name,
        phone: data.phone,
        password: data.password,
        role: data.role,
        documentNumber: data.documentNumber,
        documentType: data.documentType as 'CPF' | 'CNPJ',
      });
      
      login(response.user, response.accessToken);
      success('Conta criada!', 'Bem-vindo ao marketplace');
      
      navigate('/verify-phone');
    } catch (err) {
      console.error('Registration failed', err);

      let errorMessage = 'Falha ao cadastrar. Tente novamente.';
      
      if (axios.isAxiosError(err)) {
        errorMessage = getFriendlyAuthErrorMessage('register', err.response?.status, err.response?.data) 
          || err.message 
          || errorMessage;
      }

      setServerError(errorMessage);
      showError('Erro no cadastro', errorMessage);
    }
  };

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setValue('phone', formatBrazilPhone(e.target.value));
  };

  const generateDocument = () => {
    const docType = documentType === 'CPF' ? generateTestCpf() : generateTestCnpj();
    setValue('documentNumber', docType);
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-ink p-4 text-zinc-200 font-sans">
      <div className="auction-panel w-full max-w-sm space-y-8 bg-ink/50 p-8 shadow-xl">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-serif text-citrus">Criar Conta</h1>
          <p className="text-zinc-400 text-sm">Cadastre-se para participar</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {serverError ? (
            <div className="rounded-lg border border-danger/40 bg-danger/10 px-3 py-2 text-sm text-zinc-100">
              <div className="font-mono text-xs text-danger">CADASTRO_FALHOU</div>
              <div className="mt-1">{serverError}</div>
            </div>
          ) : null}

          <div className="flex justify-center space-x-4 pb-2">
            <button
              type="button"
              onClick={() => setValue('role', 'buyer')}
              className={`px-4 py-2 rounded-md transition-colors ${
                watch('role') === 'buyer' 
                  ? 'bg-citrus text-ink font-bold' 
                  : 'bg-ink border border-stroke text-zinc-400'
              }`}
            >
              Comprador
            </button>
            <button
              type="button"
              onClick={() => setValue('role', 'supplier')}
              className={`px-4 py-2 rounded-md transition-colors ${
                watch('role') === 'supplier' 
                  ? 'bg-citrus text-ink font-bold' 
                  : 'bg-ink border border-stroke text-zinc-400'
              }`}
            >
              Vendedor
            </button>
          </div>

          <div className="space-y-1">
            <label htmlFor="name" className="block text-sm font-medium text-zinc-300">
              Nome Completo / Empresa <span className="text-red-400">*</span>
            </label>
            <input
              id="name"
              type="text"
              placeholder="Seu nome"
              {...register('name')}
              className={`
                w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500
                focus:outline-none focus:ring-2 focus:ring-citrus focus:border-transparent
                ${errors.name ? 'border-red-500' : 'border-stroke'}
              `}
            />
            {errors.name && (
              <p className="text-sm text-red-400">{errors.name.message}</p>
            )}
          </div>

          <div className="space-y-1">
            <label htmlFor="phone" className="block text-sm font-medium text-zinc-300">
              Telefone / WhatsApp <span className="text-red-400">*</span>
            </label>
            <input
              id="phone"
              type="text"
              placeholder="(11) 99999-9999"
              {...register('phone')}
              onChange={handlePhoneChange}
              className={`
                w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500
                focus:outline-none focus:ring-2 focus:ring-citrus focus:border-transparent
                ${errors.phone ? 'border-red-500' : 'border-stroke'}
              `}
            />
            {errors.phone && (
              <p className="text-sm text-red-400">{errors.phone.message}</p>
            )}
            <p className="text-xs text-zinc-500 -mt-2">
              No MVP, o acesso é centrado em telefone/WhatsApp.
            </p>
          </div>

          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="text-sm text-zinc-300">Documento <span className="text-red-400">*</span></label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setValue('documentType', 'CPF')}
                  className={`px-3 py-1 rounded-md text-xs transition-colors ${
                    documentType === 'CPF' 
                      ? 'bg-citrus text-ink font-bold' 
                      : 'bg-ink border border-stroke text-zinc-400'
                  }`}
                >
                  CPF
                </button>
                <button
                  type="button"
                  onClick={() => setValue('documentType', 'CNPJ')}
                  className={`px-3 py-1 rounded-md text-xs transition-colors ${
                    documentType === 'CNPJ' 
                      ? 'bg-citrus text-ink font-bold' 
                      : 'bg-ink border border-stroke text-zinc-400'
                  }`}
                >
                  CNPJ
                </button>
              </div>
            </div>

            <div className="flex gap-2">
              <input
                id="documentNumber"
                type="text"
                placeholder={documentType === 'CPF' ? 'CPF válido (11 dígitos)' : 'CNPJ válido (14 dígitos)'}
                {...register('documentNumber')}
                className={`
                  flex-1 px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500
                  focus:outline-none focus:ring-2 focus:ring-citrus focus:border-transparent
                  ${errors.documentNumber ? 'border-red-500' : 'border-stroke'}
                `}
              />
              <button
                type="button"
                onClick={generateDocument}
                className="px-3 py-2 text-xs text-citrus hover:underline border border-stroke rounded"
              >
                gerar
              </button>
            </div>
            {errors.documentNumber && (
              <p className="text-sm text-red-400">{errors.documentNumber.message}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <label htmlFor="password" className="block text-sm font-medium text-zinc-300">
                Senha <span className="text-red-400">*</span>
              </label>
              <input
                id="password"
                type="password"
                placeholder="•••••••"
                {...register('password')}
                className={`
                  w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500
                  focus:outline-none focus:ring-2 focus:ring-citrus focus:border-transparent
                  ${errors.password ? 'border-red-500' : 'border-stroke'}
                `}
              />
              {errors.password && (
                <p className="text-sm text-red-400">{errors.password.message}</p>
              )}
            </div>
            <div className="space-y-1">
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-zinc-300">
                Confirmar <span className="text-red-400">*</span>
              </label>
              <input
                id="confirmPassword"
                type="password"
                placeholder="•••••••"
                {...register('confirmPassword')}
                className={`
                  w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500
                  focus:outline-none focus:ring-2 focus:ring-citrus focus:border-transparent
                  ${errors.confirmPassword ? 'border-red-500' : 'border-stroke'}
                `}
              />
              {errors.confirmPassword && (
                <p className="text-sm text-red-400">{errors.confirmPassword.message}</p>
              )}
            </div>
          </div>

          <Button
            type="submit"
            className="w-full"
            isLoading={isSubmitting}
            size="lg"
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