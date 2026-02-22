import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useAuth } from '../../context/useAuth';
import { ShieldCheck } from 'lucide-react';
import { markPhoneVerified } from '../../lib/onboarding';

export default function PhoneVerification() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [code, setCode] = useState('');
  const [error, setError] = useState<string | null>(null);

  const maskedIdentifier = (() => {
    if (!user?.email) return 'seu telefone';
    const [localPart] = user.email.split('@');
    if (!localPart) return 'seu telefone';

    const digits = localPart.replace(/\D/g, '');
    if (digits.length < 4) return 'seu telefone';

    const tail = digits.slice(-4);
    return `***-***-${tail}`;
  })();

  const handleConfirm = (e: React.FormEvent) => {
    e.preventDefault();

    if (!/^\d{6}$/.test(code)) {
      setError('Digite um código de 6 dígitos.');
      return;
    }

    setError(null);
    markPhoneVerified();
    navigate('/onboarding/profile');
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-ink p-4 text-zinc-200 font-sans">
      <div className="auction-panel w-full max-w-sm space-y-8 bg-ink/50 p-8 shadow-xl">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-serif text-citrus">Verificação</h1>
          <p className="text-zinc-400 text-sm">
            Enviamos um código para <span className="font-mono text-zinc-300">{maskedIdentifier}</span>
          </p>
        </div>

        <form onSubmit={handleConfirm} className="space-y-6">
          <Input
            id="verificationCode"
            label="Código (SMS/WhatsApp)"
            inputMode="numeric"
            maxLength={6}
            autoComplete="one-time-code"
            placeholder="123456"
            value={code}
            onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
            error={error ?? undefined}
            required
          />

          <p className="text-xs text-zinc-500 leading-relaxed">
            MVP: tela de verificação em modo simulado (UI). A integração real de envio/validação do OTP será feita na próxima etapa.
          </p>

          <Button type="submit" className="w-full" size="lg">
            <ShieldCheck className="mr-2 h-4 w-4" />
            Confirmar código
          </Button>
        </form>

        <div className="text-center text-sm text-zinc-500">
          Não recebeu?{' '}
          <Link to="/support" className="text-citrus hover:underline font-medium">
            Falar com suporte
          </Link>
        </div>
      </div>
    </div>
  );
}
