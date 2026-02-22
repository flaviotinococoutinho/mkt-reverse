import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, CircleCheck } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../context/useAuth';
import { getRoleDashboardPath } from '../../lib/onboarding';

type TutorialStep = {
  title: string;
  description: string;
};

const BUYER_STEPS: TutorialStep[] = [
  {
    title: '1) Publique sua necessidade',
    description: 'Descreva o que você procura com detalhes claros para aumentar qualidade das propostas.',
  },
  {
    title: '2) Compare propostas',
    description: 'Avalie preço, prazo e condições. Negocie com segurança antes de tomar decisão.',
  },
  {
    title: '3) Aceite a melhor opção',
    description: 'Confirme a proposta escolhida e siga para as próximas etapas de contratação.',
  },
];

const SUPPLIER_STEPS: TutorialStep[] = [
  {
    title: '1) Descubra oportunidades',
    description: 'Use filtros por categoria, preço e localização para encontrar solicitações aderentes ao seu perfil.',
  },
  {
    title: '2) Envie proposta competitiva',
    description: 'Detalhe produto, frete, prazo e condições para aumentar sua taxa de aceite.',
  },
  {
    title: '3) Acompanhe até a conclusão',
    description: 'Gerencie propostas pendentes/aceitas e mantenha comunicação clara com o comprador.',
  },
];

export default function OnboardingTutorial() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [index, setIndex] = useState(0);

  const steps = user?.role === 'supplier' ? SUPPLIER_STEPS : BUYER_STEPS;

  const current = steps[index];
  const isLast = index === steps.length - 1;

  const dashboardPath = useMemo(
    () => getRoleDashboardPath(user),
    [user]
  );

  const goNext = () => {
    if (isLast) {
      localStorage.setItem('onboardingTutorialDone', 'true');
      navigate(dashboardPath);
      return;
    }

    setIndex((value) => value + 1);
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-ink p-4 text-zinc-200 font-sans">
      <div className="auction-panel w-full max-w-xl space-y-8 bg-ink/50 p-8 shadow-xl">
        <div className="space-y-2 text-center">
          <h1 className="text-3xl font-serif text-citrus">Tutorial rápido</h1>
          <p className="text-sm text-zinc-400">
            {user?.role === 'supplier'
              ? 'Entenda o fluxo do vendedor no MVP em 3 passos.'
              : 'Entenda o fluxo do comprador no MVP em 3 passos.'}
          </p>
        </div>

        <div className="rounded-xl border border-stroke bg-paper/5 p-6">
          <p className="mb-2 text-xs font-mono uppercase tracking-widest text-zinc-500">Passo {index + 1} de 3</p>
          <h2 className="text-xl font-semibold text-zinc-100">{current.title}</h2>
          <p className="mt-3 text-sm leading-relaxed text-zinc-300">{current.description}</p>
        </div>

        <div className="flex items-center justify-between">
          <Button
            type="button"
            variant="ghost"
            onClick={() => {
              localStorage.setItem('onboardingTutorialDone', 'true');
              navigate(dashboardPath);
            }}
          >
            Pular tutorial
          </Button>

          <Button type="button" onClick={goNext}>
            {isLast ? (
              <>
                <CircleCheck className="mr-2 h-4 w-4" />
                Ir para dashboard
              </>
            ) : (
              <>
                Próximo
                <ArrowRight className="ml-2 h-4 w-4" />
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
}
