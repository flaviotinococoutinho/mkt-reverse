import React from 'react';
import { Inbox, Search, FileX, Users } from 'lucide-react';
import { Button } from '../Button';

type EmptyStateVariant = 'default' | 'search' | 'no-results' | 'no-users';

interface EmptyStateProps {
  variant?: EmptyStateVariant;
  title: string;
  description?: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

const icons = {
  default: Inbox,
  search: Search,
  'no-results': FileX,
  'no-users': Users,
};

const messages = {
  default: {
    icon: Inbox,
    title: 'Nenhum item encontrado',
    description: 'Não há dados para exibir no momento.',
  },
  search: {
    icon: Search,
    title: 'Nenhum resultado',
    description: 'Tente buscar com outros termos.',
  },
  'no-results': {
    icon: FileX,
    title: 'Sem resultados',
    description: 'Não encontramos o que você procura.',
  },
  'no-users': {
    icon: Users,
    title: 'Nenhum usuário',
    description: 'Não há usuários para exibir.',
  },
};

export const EmptyState: React.FC<EmptyStateProps> = ({ 
  variant = 'default',
  title,
  description,
  action 
}) => {
  const Icon = icons[variant];

  return (
    <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
      <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
        <Icon className="w-8 h-8 text-gray-400" />
      </div>
      
      <h3 className="text-lg font-medium text-gray-900 mb-2">
        {title}
      </h3>
      
      {description && (
        <p className="text-gray-500 max-w-sm mb-6">
          {description}
        </p>
      )}
      
      {action && (
        <Button onClick={action.onClick}>
          {action.label}
        </Button>
      )}
    </div>
  );
};

// Pre-configured empty states for common use cases
export const NoData: React.FC<{ message?: string; action?: { label: string; onClick: () => void } }> = ({ 
  message = 'Nenhum dado encontrado',
  action 
}) => (
  <EmptyState 
    variant="default"
    title={message}
    action={action}
  />
);

export const NoResults: React.FC<{ action?: { label: string; onClick: () => void } }> = ({ action }) => (
  <EmptyState 
    variant="search"
    title="Nenhum resultado encontrado"
    description="Tente buscar com outros termos ou filtros."
    action={action}
  />
);