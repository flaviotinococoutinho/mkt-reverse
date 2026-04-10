import { Link } from 'react-router-dom';

export default function Support() {
  return (
    <div className="min-h-screen bg-ink text-zinc-200">
      <div className="mx-auto max-w-2xl p-6">
        <header className="mb-8">
          <h1 className="text-3xl font-serif text-citrus">Suporte</h1>
          <p className="mt-2 text-sm text-zinc-400">
            MVP: não existe recuperação automática de senha por e-mail.
          </p>
        </header>

        <section className="space-y-4 rounded-xl border border-stroke bg-paper p-6">
          <div>
            <h2 className="text-lg font-serif text-citrus">Como recuperar acesso</h2>
            <ul className="mt-2 list-disc space-y-2 pl-5 text-sm text-zinc-300">
              <li>Se você estiver em demo interna, peça para o admin resetar sua senha.</li>
              <li>Se você estiver testando o fluxo, crie um novo usuário no cadastro.</li>
            </ul>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to="/login"
              className="inline-flex items-center justify-center rounded-lg border border-stroke bg-ink/40 px-4 py-2 text-sm text-zinc-200 hover:bg-ink/60"
            >
              Voltar ao login
            </Link>
            <Link
              to="/register"
              className="inline-flex items-center justify-center rounded-lg bg-citrus px-4 py-2 text-sm font-medium text-ink hover:opacity-90"
            >
              Criar conta
            </Link>
          </div>
        </section>
      </div>
    </div>
  );
}
