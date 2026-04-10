import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserRoundPlus } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { markProfileDone } from '../../lib/onboarding';

export default function ProfileSetup() {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [location, setLocation] = useState('');
  const [notifications, setNotifications] = useState<'whatsapp' | 'sms' | 'both'>('whatsapp');
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim() || !location.trim()) {
      setError('Preencha nome e localização para continuar.');
      return;
    }

    localStorage.setItem(
      'onboardingProfile',
      JSON.stringify({
        name: name.trim(),
        location: location.trim(),
        notifications,
      })
    );

    markProfileDone();

    navigate('/onboarding/tutorial');
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-ink p-4 text-zinc-200 font-sans">
      <div className="auction-panel w-full max-w-xl space-y-8 bg-ink/50 p-8 shadow-xl">
        <div className="space-y-2 text-center">
          <h1 className="text-3xl font-serif text-citrus">Configuração de perfil</h1>
          <p className="text-sm text-zinc-400">
            Falta pouco. Complete seu perfil para receber propostas mais relevantes.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <Input
            id="name"
            label="Nome"
            placeholder="Como você quer ser chamado"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />

          <Input
            id="location"
            label="Localização"
            placeholder="Cidade, UF"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            required
          />

          <div className="space-y-2">
            <label htmlFor="notifications" className="text-sm font-medium leading-none text-zinc-400">
              Preferência de notificação
            </label>
            <select
              id="notifications"
              value={notifications}
              onChange={(e) => setNotifications(e.target.value as 'whatsapp' | 'sms' | 'both')}
              className="h-10 w-full rounded-md border border-stroke bg-ink px-3 py-2 text-sm text-zinc-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-citrus"
            >
              <option value="whatsapp">WhatsApp</option>
              <option value="sms">SMS</option>
              <option value="both">WhatsApp + SMS</option>
            </select>
          </div>

          {error && <p className="text-xs text-danger">{error}</p>}

          <p className="text-xs text-zinc-500">
            MVP: os dados são salvos localmente para manter o fluxo de onboarding até a integração completa do backend.
          </p>

          <Button type="submit" className="w-full" size="lg">
            <UserRoundPlus className="mr-2 h-4 w-4" />
            Salvar e continuar
          </Button>
        </form>
      </div>
    </div>
  );
}
