import { Link } from 'react-router-dom';
import { Gavel } from 'lucide-react';

import { Button } from '../ui/Button';
import { useAuth } from '../../context/useAuth';

export function AppHeader({
  backTo,
  backLabel = 'Voltar',
}: {
  backTo?: string;
  backLabel?: string;
}) {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <header className="border-b border-stroke">
      <div className="container mx-auto px-4 py-4 flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          {backTo ? (
            <Link
              to={backTo}
              className="text-sm text-zinc-400 hover:text-zinc-200 transition-colors"
            >
              ← {backLabel}
            </Link>
          ) : null}

          <div className="hidden sm:flex items-center gap-3">
            <div className="grid h-9 w-9 place-items-center rounded-lg border border-stroke bg-paper">
              <Gavel className="h-4 w-4 text-citrus" aria-hidden="true" />
            </div>
            <div className="leading-tight">
              <div className="font-serif text-zinc-100">QueroJá</div>
              <div className="font-mono text-[11px] text-zinc-500">O Leilão</div>
            </div>
          </div>
        </div>

        {isAuthenticated ? (
          <div className="flex items-center gap-3">
            <div className="hidden md:block text-right">
              <div className="text-xs text-zinc-500">logado como</div>
              <div className="text-sm text-zinc-300 truncate max-w-[220px]">
                {user?.name}
              </div>
            </div>
            <Button variant="ghost" size="sm" onClick={logout}>
              Sair
            </Button>
          </div>
        ) : (
          <div className="flex items-center gap-2">
            <Link to="/support" className="text-sm text-zinc-400 hover:text-zinc-200">
              Suporte
            </Link>
            <Link to="/login">
              <Button variant="ghost" size="sm">Entrar</Button>
            </Link>
          </div>
        )}
      </div>
    </header>
  );
}

