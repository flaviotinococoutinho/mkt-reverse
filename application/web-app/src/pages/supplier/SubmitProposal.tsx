import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { AppHeader } from '../../components/layout/AppHeader';
import { sourcingService } from '../../services/sourcingService';
import type { SupplierResponseRequest, SourcingEventView } from '../../services/sourcingService';
import { useToast } from '../../context/useToast';
import { useAuth } from '../../context/useAuth';
import { CONDITION_OPTIONS, SHIPPING_MODE_OPTIONS } from '../../lib/offerTerms';
import {
  DollarSign,
  Truck,
  Shield,
  CheckCircle,
  AlertCircle,
} from 'lucide-react';

export default function SubmitProposal() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { toast } = useToast();
  const { user } = useAuth();
  const [loading, setLoading] = React.useState(false);
  const [submitting, setSubmitting] = React.useState(false);
  const [event, setEvent] = React.useState<SourcingEventView | null>(null);

  const [formData, setFormData] = React.useState<SupplierResponseRequest>({
    supplierId: '',
    offerCents: 0,
    leadTimeDays: 7,
    warrantyMonths: 12,
    condition: 'NEW',
    shippingMode: 'SELLER',
    message: '',
    attributes: [],
  });

  const [formErrors, setFormErrors] = React.useState<Record<string, string>>({});

  const loadEvent = React.useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const eventData = await sourcingService.getSourcingEvent(id);
      setEvent(eventData);
    } catch (error) {
      console.error('Failed to load event:', error);
      setFormErrors({ submit: 'Falha ao carregar detalhes da solicitação' });
    } finally {
      setLoading(false);
    }
  }, [id]);

  React.useEffect(() => {
    void loadEvent();
    const currentUserId = typeof user?.id === 'string' ? user.id : '';
    if (currentUserId) {
      setFormData((prev) => ({ ...prev, supplierId: currentUserId }));
    } else {
      setFormErrors((prev) => ({
        ...prev,
        submit: 'Sessão inválida para vendedor. Faça login novamente para enviar propostas.',
      }));
    }
  }, [loadEvent, user?.id]);

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (formData.offerCents <= 0) {
      errors.offerCents = 'O valor da oferta deve ser maior que zero';
    }

    if (!formData.leadTimeDays || formData.leadTimeDays <= 0) {
      errors.leadTimeDays = 'O prazo de entrega deve ser maior que zero';
    }

    if (!formData.warrantyMonths || formData.warrantyMonths < 0) {
      errors.warrantyMonths = 'A garantia deve ser zero ou positiva';
    }

    if (!formData.supplierId || formData.supplierId.trim().length === 0) {
      errors.submit = 'Não foi possível identificar o vendedor autenticado. Faça login novamente.';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm() || !id) return;

    setSubmitting(true);
    try {
      await sourcingService.submitResponse(id, formData);
      toast({
        level: 'success',
        title: 'Proposta enviada',
        description: 'O comprador já consegue ver sua oferta no painel.',
      });

      navigate(`/supplier/opportunities/${id}`);
    } catch (error: unknown) {
      console.error('Failed to submit proposal:', error);
      type HttpErrorLike = {
        response?: {
          data?: {
            message?: unknown;
          };
        };
      };

      const maybeHttpError = error as HttpErrorLike;
      const message =
        typeof maybeHttpError.response?.data?.message === 'string'
          ? maybeHttpError.response?.data?.message
          : 'Falha ao enviar proposta. Tente novamente.';

      setFormErrors({ submit: message });
    } finally {
      setSubmitting(false);
    }
  };

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;

    const asPositiveInteger = (raw: string): number => {
      const parsed = Number.parseInt(raw, 10);
      return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0;
    };

    const asMoneyCents = (raw: string): number => {
      const normalized = raw.replace(',', '.');
      const parsed = Number.parseFloat(normalized);
      if (!Number.isFinite(parsed) || parsed <= 0) return 0;
      return Math.round(parsed * 100);
    };

    setFormData((prev) => ({
      ...prev,
      [name]:
        name === 'offerCents'
          ? asMoneyCents(value)
          : name === 'leadTimeDays' || name === 'warrantyMonths'
            ? asPositiveInteger(value)
            : value,
    }));
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-ink text-zinc-200 font-sans flex items-center justify-center">
        <div className="text-zinc-400">Carregando...</div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="min-h-screen bg-ink text-zinc-200 font-sans flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="h-16 w-16 mx-auto mb-4 text-zinc-600" />
          <p className="text-zinc-400 mb-4">Solicitação não encontrada</p>
          <Button onClick={() => navigate('/supplier/opportunities')}>Voltar</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-ink text-zinc-200 font-sans">
      <AppHeader
        backTo={id ? `/supplier/opportunities/${id}` : '/supplier/opportunities'}
        backLabel="Oportunidades"
      />

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        <div className="max-w-3xl mx-auto">
          {/* Page Title */}
          <div className="mb-8">
            <h2 className="text-3xl font-serif text-zinc-100 mb-2">Enviar Proposta</h2>
            <p className="text-zinc-400">Ofereça seus produtos ou serviços para esta solicitação</p>
          </div>

          {/* Event Summary */}
          <div className="auction-panel p-6 mb-8">
            <h3 className="text-lg font-serif text-zinc-100 mb-4">Detalhes da Solicitação</h3>
            <div className="space-y-3">
              <div>
                <p className="text-sm text-zinc-500">Título</p>
                <p className="text-zinc-200 font-medium">{event.title}</p>
              </div>
              {event.description && (
                <div>
                  <p className="text-sm text-zinc-500">Descrição</p>
                  <p className="text-zinc-200">{event.description}</p>
                </div>
              )}
              <div>
                <p className="text-sm text-zinc-500">Tipo</p>
                <p className="text-zinc-200">{event.eventType}</p>
              </div>
              <div>
                <p className="text-sm text-zinc-500">Status</p>
                <StatusBadge status={event.status} />
              </div>
            </div>
          </div>

          {/* Proposal Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="auction-panel p-6">
              <h3 className="text-lg font-serif text-zinc-100 mb-4">Sua Proposta</h3>

              {formErrors.submit && (
                <div className="auction-panel bg-danger/10 border-danger/50 rounded-lg p-4 mb-6">
                  <p className="text-danger text-sm">{formErrors.submit}</p>
                </div>
              )}

              <div className="space-y-6">
                {/* Price */}
                <div>
                  <label className="block text-sm font-medium text-zinc-300 mb-2">
                    Preço da Oferta (R$) *
                  </label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-zinc-500" />
                    <Input
                      type="number"
                      name="offerCents"
                      placeholder="0,00"
                      step="0.01"
                      min="0"
                      value={(formData.offerCents / 100).toFixed(2)}
                      onChange={handleInputChange}
                      className="pl-10"
                      required
                    />
                  </div>
                  {formErrors.offerCents && (
                    <p className="text-danger text-sm mt-1">{formErrors.offerCents}</p>
                  )}
                </div>

                {/* Lead Time */}
                <div>
                  <label className="block text-sm font-medium text-zinc-300 mb-2">
                    Prazo de Entrega (dias) *
                  </label>
                  <div className="relative">
                    <Truck className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-zinc-500" />
                    <Input
                      type="number"
                      name="leadTimeDays"
                      placeholder="7"
                      min="1"
                      value={formData.leadTimeDays}
                      onChange={handleInputChange}
                      className="pl-10"
                      required
                    />
                  </div>
                  {formErrors.leadTimeDays && (
                    <p className="text-danger text-sm mt-1">{formErrors.leadTimeDays}</p>
                  )}
                </div>

                {/* Warranty */}
                <div>
                  <label className="block text-sm font-medium text-zinc-300 mb-2">
                    Garantia (meses) *
                  </label>
                  <div className="relative">
                    <Shield className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-zinc-500" />
                    <Input
                      type="number"
                      name="warrantyMonths"
                      placeholder="12"
                      min="0"
                      value={formData.warrantyMonths}
                      onChange={handleInputChange}
                      className="pl-10"
                      required
                    />
                  </div>
                  {formErrors.warrantyMonths && (
                    <p className="text-danger text-sm mt-1">{formErrors.warrantyMonths}</p>
                  )}
                </div>

                {/* Condition */}
                <div>
                  <label className="block text-sm font-medium text-zinc-300 mb-2">
                    Condição do Produto *
                  </label>
                  <select
                    name="condition"
                    value={formData.condition}
                    onChange={handleInputChange}
                    className="w-full bg-ink border border-stroke rounded-md px-4 py-3 text-zinc-200 focus:border-citrus focus:outline-none"
                    required
                  >
                    {CONDITION_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Shipping Mode */}
                <div>
                  <label className="block text-sm font-medium text-zinc-300 mb-2">
                    Modalidade de Frete *
                  </label>
                  <select
                    name="shippingMode"
                    value={formData.shippingMode}
                    onChange={handleInputChange}
                    className="w-full bg-ink border border-stroke rounded-md px-4 py-3 text-zinc-200 focus:border-citrus focus:outline-none"
                    required
                  >
                    {SHIPPING_MODE_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Message */}
                <div>
                  <label className="block text-sm font-medium text-zinc-300 mb-2">
                    Mensagem Adicional
                  </label>
                  <textarea
                    name="message"
                    value={formData.message}
                    onChange={handleInputChange}
                    rows={4}
                    placeholder="Adicione detalhes adicionais sobre sua proposta..."
                    className="w-full bg-ink border border-stroke rounded-md px-4 py-3 text-zinc-200 focus:border-citrus focus:outline-none resize-none"
                  />
                </div>
              </div>
            </div>

            {/* Form Actions */}
            <div className="flex items-center gap-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate(id ? `/supplier/opportunities/${id}` : '/supplier/opportunities')}
                disabled={submitting}
              >
                Cancelar
              </Button>
              <Button
                type="submit"
                size="lg"
                isLoading={submitting}
                disabled={!formData.supplierId}
              >
                <CheckCircle className="mr-2 h-5 w-5" />
                Enviar Proposta
              </Button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
