import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { useAuth } from '../../context/useAuth';
import { AppHeader } from '../../components/layout/AppHeader';
import { sourcingService } from '../../services/sourcingService';
import type { SourcingEventView } from '../../services/sourcingService';
import {
  CheckCircle,
  Clock,
  TrendingUp,
  AlertCircle,
  Search,
} from 'lucide-react';
import { Loading, ListSkeleton, NoData } from '../../components/ui/feedback';
import { useToast } from '../../components/ui/feedback';

type IconComponent = React.ComponentType<{ className?: string }>;

interface Stats {
  totalOpportunities: number;
  activeProposals: number;
  acceptedProposals: number;
}

export default function SupplierDashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { error: showError } = useToast();

  const [opportunities, setOpportunities] = React.useState<SourcingEventView[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);
  const [stats, setStats] = React.useState<Stats>({
    totalOpportunities: 0,
    activeProposals: 0,
    acceptedProposals: 0,
  });

  const loadData = React.useCallback(async () => {
    setError(null);

    try {
      const result = await sourcingService.getOpportunities({
        supplierId: user?.id,
        visibility: 'PUBLIC',
        page: 0,
        size: 50,
      });

      setOpportunities(result.items);

      // Get responses for each opportunity
      const responseLists = await Promise.all(
        result.items.map((event) => sourcingService.getResponses(event.id).catch(() => []))
      );

      const supplierResponses = responseLists
        .flat()
        .filter((response) => response.supplierId === user?.id);

      const acceptedProposals = supplierResponses.filter(
        (response) => response.status === 'AWARDED' || response.status === 'ACCEPTED'
      ).length;

      const activeProposals = supplierResponses.filter(
        (response) => response.status === 'SUBMITTED'
      ).length;

      setStats({
        totalOpportunities: result.total,
        activeProposals,
        acceptedProposals,
      });
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
      setError('Não foi possível carregar os dados. Tente novamente.');
      showError('Erro ao carregar', 'Não foi possível carregar os dados do dashboard.');
    } finally {
      setLoading(false);
    }
  }, [user?.id, showError]);

  React.useEffect(() => {
    void loadData();
  }, [loadData]);

  const StatCard = ({
    icon: Icon,
    label,
    value,
  }: {
    icon: IconComponent;
    label: string;
    value: number;
  }) => (
    <div className="auction-panel p-6">
      <div className="flex items-center justify-between mb-4">
        <Icon className="h-6 w-6 text-citrus" />
        <span className="text-3xl font-serif text-zinc-100">{value}</span>
      </div>
      <p className="text-sm text-zinc-400">{label}</p>
    </div>
  );

  return (
    <div className="min-h-screen bg-ink text-zinc-200 font-sans">
      <AppHeader />

      <main className="container mx-auto px-4 py-8">
        {/* Page Title */}
        <div className="mb-8">
          <h2 className="text-3xl font-serif text-zinc-100 mb-2">Dashboard do Vendedor</h2>
          <p className="text-zinc-400">Encontre oportunidades e gerencie suas propostas</p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
          <StatCard
            icon={TrendingUp}
            label="Oportunidades Disponíveis"
            value={stats.totalOpportunities}
          />
          <StatCard
            icon={Clock}
            label="Propostas Pendentes"
            value={stats.activeProposals}
          />
          <StatCard
            icon={CheckCircle}
            label="Propostas Aceitas"
            value={stats.acceptedProposals}
          />
        </div>

        {/* Opportunities Section */}
        <div className="mb-8">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-serif text-zinc-100">Oportunidades Recentes</h3>
            <Button onClick={() => navigate('/supplier/opportunities')}>
              Ver Todas
            </Button>
          </div>

          {loading ? (
            <div className="auction-panel p-8">
              <ListSkeleton count={3} />
            </div>
          ) : error ? (
            <div className="auction-panel text-center py-12 px-6">
              <AlertCircle className="h-12 w-12 mx-auto mb-4 text-red-500" />
              <p className="text-zinc-300 mb-6">{error}</p>
              <Button variant="secondary" onClick={() => { setLoading(true); void loadData(); }}>
                Tentar novamente
              </Button>
            </div>
          ) : opportunities.length === 0 ? (
            <NoData 
              message="Nenhuma oportunidade encontrada" 
              action={{ 
                label: 'Buscar Oportunidades', 
                onClick: () => navigate('/supplier/opportunities') 
              }} 
            />
          ) : (
            <div className="space-y-4">
              {opportunities.slice(0, 5).map((event) => (
                <div
                  key={event.id}
                  className="auction-panel p-6 hover:border-stroke/50 transition-colors"
                >
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex-1">
                      <h3 className="text-xl font-serif text-zinc-100 mb-1">{event.title}</h3>
                      <p className="text-sm text-zinc-400 mb-2">
                        {event.description || 'Sem descrição'}
                      </p>
                      <div className="flex items-center gap-2">
                        <StatusBadge status={event.status} />
                      </div>
                    </div>
                    <div className="text-right ml-4">
                      <p className="text-xs text-zinc-500 mb-1">ID da Solicitação</p>
                      <p className="text-sm font-mono text-zinc-400">{event.id.slice(0, 8)}...</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-4 pt-4 border-t border-stroke">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => navigate(`/supplier/opportunities/${event.id}`)}
                    >
                      Ver Detalhes
                    </Button>
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => navigate(`/supplier/submit-proposal/${event.id}`)}
                    >
                      Enviar Proposta
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}