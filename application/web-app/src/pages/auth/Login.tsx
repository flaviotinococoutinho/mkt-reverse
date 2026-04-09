import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '../../components/ui/Button';
import { LogIn } from 'lucide-react';
import { authService } from '../../services/authService';
import { useAuth } from '../../context/useAuth';
import axios from 'axios';
import { formatBrazilPhone } from '../../lib/phone';
import { getNextOnboardingPath, getRoleDashboardPath } from '../../lib/onboarding';
import { getFriendlyAuthErrorMessage } from '../../lib/authErrorMessage';
import { loginSchema, LoginFormData } from '../../lib/schemas/auth';
import { useToast } from '../../components/ui/feedback';

const AUTH_NOTICE_KEY = 'auth.notice';

export default function Login() {
  const [serverError, setServerError] = React.useState<string | null>(null);
  const navigate = useNavigate();
  const { login } = useAuth();
  const { error: showError, success } = useToast();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      identifier: '',
      password: '',
    },
  });

  React.useEffect(() => {
    const authNotice = sessionStorage.getItem(AUTH_NOTICE_KEY);
    if (authNotice) {
      setServerError(authNotice);
      sessionStorage.removeItem(AUTH_NOTICE_KEY);
    }
  }, []);

  const onSubmit = async (data: LoginFormData) => {
    setServerError(null);
    
    try {
      const response = await authService.login({ 
        identifier: data.identifier, 
        password: data.password 
      });
      
      login(response.user, response.accessToken);
      success('Bem-vindo!', `Logado como ${response.user.name}`);
      
      const redirectPath = getNextOnboardingPath() ?? getRoleDashboardPath(response.user);
      navigate(redirectPath);
    } catch (err) {
      console.error('Login failed', err);

      let errorMessage = 'Falha ao entrar.';
      
      if (axios.isAxiosError(err)) {
        errorMessage = getFriendlyAuthErrorMessage('login', err.response?.status, err.response?.data) 
          || err.message 
          || errorMessage;
      }

      setServerError(errorMessage);
      showError('Erro ao登录', errorMessage);
    }
  };

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setValue('identifier', formatBrazilPhone(e.target.value));
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-ink p-4 text-zinc-200 font-sans">
      <div className="auction-panel w-full max-w-sm space-y-8 bg-ink/50 p-8 shadow-xl">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-serif text-citrus">O Leilão</h1>
          <p className="text-zinc-400 text-sm">Entre para acessar o pregão</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {serverError ? (
            <div className="rounded-lg border border-danger/40 bg-danger/10 px-3 py-2 text-sm text-zinc-100">
              <div className="font-mono text-xs text-danger">LOGIN_FALHOU</div>
              <div className="mt-1">{serverError}</div>
            </div>
          ) : null}

          <div className="space-y-1">
            <label htmlFor="identifier" className="block text-sm font-medium text-zinc-300">
              Telefone / WhatsApp <span className="text-red-400">*</span>
            </label>
            <input
              id="identifier"
              type="text"
              placeholder="(11) 99999-9999"
              {...register('identifier')}
              onChange={handlePhoneChange}
              className={`
                w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500
                focus:outline-none focus:ring-2 focus:ring-citrus focus:border-transparent
                transition-colors
                ${errors.identifier 
                  ? 'border-red-500 focus:ring-red-500' 
                  : 'border-stroke hover:border-zinc-600'
                }
              `}
              aria-invalid={!!errors.identifier}
            />
            {errors.identifier && (
              <p className="text-sm text-red-400" role="alert">
                {errors.identifier.message}
              </p>
            )}
          </div>
          
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
                transition-colors
                ${errors.password 
                  ? 'border-red-500 focus:ring-red-500' 
                  : 'border-stroke hover:border-zinc-600'
                }
              `}
              aria-invalid={!!errors.password}
            />
            {errors.password && (
              <p className="text-sm text-red-400" role="alert">
                {errors.password.message}
              </p>
            )}
            <p className="text-xs text-zinc-500">
              MVP: sem recuperação de senha por e-mail. Se precisar, fale com o suporte.
              {' '}
              <Link to="/support" className="text-citrus hover:underline">Ver opções</Link>
              .
            </p>
          </div>

          <Button 
            type="submit" 
            className="w-full" 
            isLoading={isSubmitting} 
            size="lg"
          >
            <LogIn className="mr-2 h-4 w-4" />
            Entrar
          </Button>
        </form>

        <div className="text-center text-sm text-zinc-500">
          Não tem uma conta?{' '}
          <Link to="/register" className="text-citrus hover:underline font-medium">
            Cadastre-se
          </Link>
        </div>
      </div>
    </div>
  );
}