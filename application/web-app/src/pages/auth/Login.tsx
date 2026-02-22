import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { LogIn } from 'lucide-react';
import { authService } from '../../services/authService';
import { useAuth } from '../../context/useAuth';
import axios from 'axios';
import { formatBrazilPhone } from '../../lib/phone';
import { getNextOnboardingPath, getRoleDashboardPath } from '../../lib/onboarding';
import { getFriendlyAuthErrorMessage } from '../../lib/authErrorMessage';
import { isValidBrazilPhone } from '../../lib/authValidation';

const AUTH_NOTICE_KEY = 'auth.notice';

export default function Login() {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const navigate = useNavigate();
  const { login } = useAuth();

  useEffect(() => {
    const authNotice = sessionStorage.getItem(AUTH_NOTICE_KEY);
    if (authNotice) {
      setErrorMessage(authNotice);
      sessionStorage.removeItem(AUTH_NOTICE_KEY);
    }
  }, []);

  const identifierError =
    identifier.length > 0 && !isValidBrazilPhone(identifier)
      ? 'Informe um telefone/WhatsApp válido (10 ou 11 dígitos).'
      : null;

  const passwordError =
    password.length > 0 && password.length < 8
      ? 'Senha deve ter pelo menos 8 caracteres.'
      : null;

  const isFormInvalid = !!identifierError || !!passwordError || !identifier || !password;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (isFormInvalid) {
      setErrorMessage('Revise os campos antes de continuar.');
      return;
    }

    setIsLoading(true);
    try {
      const response = await authService.login({ identifier, password });
      login(response.user, response.token);
      const redirectPath = getNextOnboardingPath() ?? getRoleDashboardPath(response.user);
      navigate(redirectPath);
    } catch (error) {
      console.error('Login failed', error);

      if (axios.isAxiosError(error)) {
        const msg = getFriendlyAuthErrorMessage('login', error.response?.status, error.response?.data);
        setErrorMessage(msg || error.message);
      } else {
        setErrorMessage('Falha ao entrar.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-ink p-4 text-zinc-200 font-sans">
      <div className="auction-panel w-full max-w-sm space-y-8 bg-ink/50 p-8 shadow-xl">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-serif text-citrus">O Leilão</h1>
          <p className="text-zinc-400 text-sm">Entre para acessar o pregão</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {errorMessage ? (
            <div className="rounded-lg border border-danger/40 bg-danger/10 px-3 py-2 text-sm text-zinc-100">
              <div className="font-mono text-xs text-danger">LOGIN_FALHOU</div>
              <div className="mt-1">{errorMessage}</div>
            </div>
          ) : null}

          <Input
            id="identifier"
            type="text"
            label="Telefone / WhatsApp"
            placeholder="(11) 99999-9999"
            value={identifier}
            onChange={(e) => setIdentifier(formatBrazilPhone(e.target.value))}
            required
            autoComplete="username"
            error={identifierError ?? undefined}
          />
          
          <div className="space-y-2">
             <Input
              id="password"
              type="password"
              label="Senha"
              placeholder="•••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
              error={passwordError ?? undefined}
            />
            <p className="text-xs text-zinc-500">
              MVP: sem recuperação de senha por e-mail. Se precisar, fale com o suporte.
              {' '}
              <Link to="/support" className="text-citrus hover:underline">Ver opções</Link>
              .
            </p>
          </div>

          <Button type="submit" className="w-full" isLoading={isLoading} size="lg" disabled={isFormInvalid}>
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
