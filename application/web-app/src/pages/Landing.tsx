import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Gavel, ShieldCheck, Zap } from 'lucide-react';

import { Button } from '../components/ui/Button';

export default function Landing() {
  return (
    <div className="min-h-screen bg-ink text-zinc-200">
      <header className="mx-auto flex w-full max-w-5xl items-center justify-between px-6 py-6">
        <div className="flex items-center gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-lg border border-stroke bg-paper">
            <Gavel className="h-5 w-5 text-citrus" aria-hidden="true" />
          </div>
          <div className="leading-tight">
            <div className="font-serif text-lg text-zinc-100">QueroJá</div>
            <div className="font-mono text-[11px] text-zinc-500">O Leilão • reverse marketplace</div>
          </div>
        </div>

        <nav className="flex items-center gap-3">
          <Link
            to="/support"
            className="text-sm text-zinc-400 hover:text-zinc-200 transition-colors"
          >
            Suporte
          </Link>
          <Link to="/login">
            <Button variant="ghost" size="sm">Entrar</Button>
          </Link>
        </nav>
      </header>

      <main className="mx-auto w-full max-w-5xl px-6 pb-16 pt-10">
        <section className="grid gap-10 lg:grid-cols-2 lg:items-center">
          <div className="space-y-6">
            <div className="inline-flex items-center gap-2 rounded-full border border-stroke bg-paper px-3 py-1 text-xs text-zinc-300">
              <span className="font-mono text-[11px] text-mint">MVP</span>
              <span className="text-zinc-400">Comprador publica • vendedores competem • você escolhe</span>
            </div>

            <h1 className="font-serif text-4xl leading-tight text-zinc-100">
              Um <span className="text-citrus">leilão ao contrário</span> para encontrar o que você quer —
              pelo melhor preço.
            </h1>

            <p className="text-zinc-400 leading-relaxed">
              Você descreve a necessidade. Os vendedores enviam propostas. Você aceita uma — com uma
              jornada pensada para ser objetiva, segura e sem ruído.
            </p>

            <div className="flex flex-col gap-3 sm:flex-row">
              <Link to="/register">
                <Button size="lg" className="w-full sm:w-auto">
                  Criar conta
                  <ArrowRight className="ml-2 h-4 w-4" aria-hidden="true" />
                </Button>
              </Link>
              <Link to="/login">
                <Button variant="secondary" size="lg" className="w-full sm:w-auto">
                  Já tenho conta
                </Button>
              </Link>
            </div>

            <p className="text-xs text-zinc-500">
              Sem e-mail no MVP. Identificador primário: telefone/WhatsApp.
            </p>
          </div>

          <div className="rounded-2xl border border-stroke bg-paper p-6">
            <div className="grid gap-4">
              <Feature
                icon={<Zap className="h-4 w-4 text-citrus" aria-hidden="true" />}
                title="Alta velocidade"
                body="Publicar uma solicitação leva menos de 1 minuto (MVP)."
              />
              <Feature
                icon={<ShieldCheck className="h-4 w-4 text-mint" aria-hidden="true" />}
                title="Confiança por padrão"
                body="Estrutura preparada para escrow e verificação (roadmap)."
              />
              <Feature
                icon={<Gavel className="h-4 w-4 text-zinc-200" aria-hidden="true" />}
                title="Industrial/editorial"
                body="UI de alto sinal: menos 'marketplace vibes', mais 'piso de compras'."
              />
            </div>
          </div>
        </section>

        <section className="mt-14 grid gap-4 rounded-2xl border border-stroke bg-ink/50 p-6">
          <div className="font-mono text-[11px] text-zinc-500">FLUXO PRINCIPAL (MVP)</div>
          <ol className="grid gap-3 sm:grid-cols-3">
            <Step n="1" label="Publica" desc="Crie uma solicitação com título, descrição e preço alvo." />
            <Step n="2" label="Recebe" desc="Vendedores enviam propostas com preço e prazo." />
            <Step n="3" label="Aceita" desc="Aceite uma proposta e avance para o próximo estágio." />
          </ol>
        </section>
      </main>
    </div>
  );
}

function Feature({
  icon,
  title,
  body,
}: {
  icon: ReactNode;
  title: string;
  body: string;
}) {
  return (
    <div className="rounded-xl border border-stroke bg-ink/40 p-4">
      <div className="flex items-center gap-2">
        <div className="grid h-8 w-8 place-items-center rounded-lg border border-stroke bg-paper">
          {icon}
        </div>
        <div className="font-serif text-zinc-100">{title}</div>
      </div>
      <div className="mt-2 text-sm text-zinc-400 leading-relaxed">{body}</div>
    </div>
  );
}

function Step({
  n,
  label,
  desc,
}: {
  n: string;
  label: string;
  desc: string;
}) {
  return (
    <li className="rounded-xl border border-stroke bg-paper p-4">
      <div className="flex items-baseline justify-between">
        <div className="font-mono text-xs text-zinc-500">PASSO {n}</div>
        <div className="font-serif text-citrus">{label}</div>
      </div>
      <div className="mt-2 text-sm text-zinc-400 leading-relaxed">{desc}</div>
    </li>
  );
}
