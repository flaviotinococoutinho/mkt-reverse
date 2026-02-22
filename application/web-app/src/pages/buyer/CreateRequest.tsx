import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useAuth } from '../../context/useAuth';
import { AppHeader } from '../../components/layout/AppHeader';
import { sourcingService } from '../../services/sourcingService';
import type { CreateSourcingEventRequest } from '../../services/sourcingService';
import { Loader2 } from 'lucide-react';
import { useToast } from '../../context/useToast';
import { getEventTypeLabel } from '../../lib/eventType';
import { formatBrlFromCents } from '../../lib/currency';
import { formatBrazilPhone } from '../../lib/phone';
import { validateCreateRequestForm } from '../../lib/createRequestValidation';
import { buildCreateRequestErrorToast } from '../../lib/createRequestSubmit';

type SourcingEventType = NonNullable<CreateSourcingEventRequest['type']>;

export default function CreateRequest() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState<'edit' | 'preview'>('edit');
  const [validationMessage, setValidationMessage] = useState<string | null>(null);

  const [formData, setFormData] = useState<CreateSourcingEventRequest>({
    tenantId: user?.tenantId || 'tenant-default',
    buyerOrganizationId: user?.organizationId || '',
    buyerContactName: user?.name || '',
    buyerContactPhone: '',
    title: '',
    description: '',
    type: 'RFQ',
    productName: '',
    productDescription: '',
    category: '',
    unitOfMeasure: 'un',
    quantityRequired: 1,
    validForHours: 168, // 7 dias
  });

  const handleSubmit = async (e?: React.FormEvent) => {
    e?.preventDefault();
    setLoading(true);

    try {
      const response = await sourcingService.createSourcingEvent(formData);
      navigate(`/sourcing-events/${response.id}`);
    } catch (error) {
      console.error('Failed to create request:', error);
      const toastContent = buildCreateRequestErrorToast(error);
      toast({
        level: 'error',
        ...toastContent,
      });
    } finally {
      setLoading(false);
    }
  };

  const {
    contactPhoneError,
    titleError,
    descriptionError,
    quantityError,
    isInvalidForPreview,
  } = validateCreateRequestForm(formData);

  return (
    <div className="min-h-screen bg-ink text-zinc-200 font-sans">
      <AppHeader backTo="/dashboard" backLabel="Dashboard" />

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto">
          {/* Page Title */}
          <div className="mb-8 text-center">
            <h1 className="text-3xl font-serif text-zinc-100 mb-2">Publicar Solicitação</h1>
            <p className="text-zinc-400">
              Descreva o produto que você precisa e receba propostas de fornecedores
            </p>
          </div>

          {step === 'edit' ? (
            /* Form */
            <form onSubmit={(e) => {
              e.preventDefault();
              if (isInvalidForPreview) {
                setValidationMessage('Revise os campos obrigatórios antes de continuar para a pré-visualização.');
                return;
              }
              setValidationMessage(null);
              setStep('preview');
            }} className="space-y-6">
            {validationMessage ? (
              <div className="rounded-lg border border-danger/40 bg-danger/10 px-3 py-2 text-sm text-zinc-100">
                {validationMessage}
              </div>
            ) : null}
            {/* Request Type */}
            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-2">
                Tipo de Solicitação
              </label>
              <select
                value={formData.type}
                onChange={(e) =>
                  setFormData({ ...formData, type: e.target.value as SourcingEventType })
                }
                className="w-full h-10 px-3 rounded-md border border-stroke bg-ink/50 text-zinc-200 focus:border-citrus focus:outline-none"
              >
                <option value="RFQ">{getEventTypeLabel('RFQ')}</option>
                <option value="REVERSE_AUCTION">{getEventTypeLabel('REVERSE_AUCTION')}</option>
              </select>
            </div>

            {/* Product Name */}
            <Input
              id="productName"
              label="Nome do Produto"
              placeholder="Ex: Laptop Dell Latitude 7490"
              value={formData.productName}
              onChange={(e) => {
                const productName = e.target.value;
                setFormData((prev) => ({
                  ...prev,
                  productName,
                  // UX: se o título ainda não foi preenchido, usamos o nome do produto como rascunho.
                  title: prev.title?.trim() ? prev.title : productName,
                }));
              }}
              required
            />

            {/* Title */}
            <Input
              id="title"
              label="Título do Pedido"
              placeholder="Ex: Quero cotação para 10 unidades do Dell Latitude 7490"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
              error={titleError ?? undefined}
            />

            {/* Product Description */}
            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-2">
                Descrição Detalhada
              </label>
              <textarea
                id="productDescription"
                placeholder="Descreva especificações técnicas, requisitos mínimos, condições especiais..."
                value={formData.productDescription || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    productDescription: e.target.value,
                    // Mantemos uma descrição genérica do pedido para listagens/preview.
                    description: e.target.value,
                  })
                }
                rows={4}
                className="w-full px-3 py-2 rounded-md border border-stroke bg-ink/50 text-zinc-200 focus:border-citrus focus:outline-none"
              />
              {descriptionError ? (
                <p className="mt-1 text-xs text-danger">{descriptionError}</p>
              ) : null}
            </div>

            {/* Category */}
            <Input
              id="category"
              label="Categoria"
              placeholder="Ex: Eletrônicos, Informática, Móveis"
              value={formData.category || ''}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
            />

            {/* Quantity and Unit */}
            <div className="grid grid-cols-2 gap-4">
              <Input
                id="quantity"
                type="number"
                label="Quantidade"
                min={1}
                value={formData.quantityRequired}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    quantityRequired: parseInt(e.target.value) || 1,
                  })
                }
                required
                error={quantityError ?? undefined}
              />
              <Input
                id="unit"
                label="Unidade de Medida"
                placeholder="Ex: un, kg, m"
                value={formData.unitOfMeasure}
                onChange={(e) =>
                  setFormData({ ...formData, unitOfMeasure: e.target.value })
                }
                required
              />
            </div>

            {/* Budget */}
            <Input
              id="budget"
              type="number"
              label="Orçamento Estimado (R$)"
              placeholder="0.00"
              step="0.01"
              min={0}
              value={
                formData.estimatedBudgetCents
                  ? (formData.estimatedBudgetCents / 100).toFixed(2)
                  : ''
              }
              onChange={(e) =>
                setFormData((prev) => {
                  const raw = e.target.value;
                  if (!raw || raw.trim() === '') {
                    return { ...prev, estimatedBudgetCents: undefined };
                  }
                  const cents = Math.round(Number.parseFloat(raw) * 100);
                  return { ...prev, estimatedBudgetCents: Number.isFinite(cents) ? cents : undefined };
                })
              }
            />

            {/* Validity Period */}
            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-2">
                Válido por (horas)
              </label>
              <select
                value={formData.validForHours}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    validForHours: parseInt(e.target.value),
                  })
                }
                className="w-full h-10 px-3 rounded-md border border-stroke bg-ink/50 text-zinc-200 focus:border-citrus focus:outline-none"
              >
                <option value={24}>24 horas</option>
                <option value={48}>48 horas</option>
                <option value={72}>3 dias</option>
                <option value={168}>7 dias</option>
                <option value={336}>14 dias</option>
              </select>
            </div>

            {/* Contact Information */}
            <div className="space-y-4 p-4 border border-stroke rounded-lg bg-ink/30">
              <h3 className="text-lg font-serif text-zinc-100 mb-3">Informações de Contato</h3>
              <Input
                id="contactName"
                label="Nome para Contato"
                value={formData.buyerContactName}
                onChange={(e) =>
                  setFormData({ ...formData, buyerContactName: e.target.value })
                }
                required
              />
              <Input
                id="contactPhone"
                type="tel"
                label="Telefone/WhatsApp"
                placeholder="(11) 99999-9999"
                value={formData.buyerContactPhone}
                onChange={(e) =>
                  setFormData({ ...formData, buyerContactPhone: formatBrazilPhone(e.target.value) })
                }
                required
                error={contactPhoneError ?? undefined}
              />
            </div>

            {/* Continue */}
            <div className="pt-4">
              <Button type="submit" size="lg" className="w-full" disabled={isInvalidForPreview}>
                Revisar antes de publicar
              </Button>
            </div>
          </form>
          ) : (
            /* Preview */
            <div className="space-y-6">
              <div className="border border-stroke rounded-xl bg-ink/50 p-6">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1">
                    <h2 className="text-2xl font-serif text-zinc-100 mb-2">Pré-visualização</h2>
                    <p className="text-zinc-400">
                      Confirme se a solicitação está clara. Depois de publicar, você poderá acompanhar as propostas no dashboard.
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs text-zinc-500 mb-1">Válido por</p>
                    <p className="text-sm font-mono text-zinc-300">{formData.validForHours}h</p>
                  </div>
                </div>

                <div className="mt-6 grid gap-4">
                  <div className="p-4 rounded-lg bg-paper border border-stroke">
                    <p className="text-xs text-zinc-500 mb-1">Título</p>
                    <p className="text-lg text-zinc-100">{formData.title || '—'}</p>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="p-4 rounded-lg bg-paper border border-stroke">
                      <p className="text-xs text-zinc-500 mb-1">Produto</p>
                      <p className="text-sm text-zinc-200">{formData.productName || '—'}</p>
                    </div>
                    <div className="p-4 rounded-lg bg-paper border border-stroke">
                      <p className="text-xs text-zinc-500 mb-1">Quantidade</p>
                      <p className="text-sm font-mono text-zinc-200">
                        {formData.quantityRequired} {formData.unitOfMeasure}
                      </p>
                    </div>
                  </div>

                  <div className="p-4 rounded-lg bg-paper border border-stroke">
                    <p className="text-xs text-zinc-500 mb-1">Descrição detalhada</p>
                    <p className="text-sm text-zinc-300 whitespace-pre-wrap">
                      {formData.productDescription || '—'}
                    </p>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="p-4 rounded-lg bg-paper border border-stroke">
                      <p className="text-xs text-zinc-500 mb-1">Categoria</p>
                      <p className="text-sm text-zinc-200">{formData.category || '—'}</p>
                    </div>
                    <div className="p-4 rounded-lg bg-paper border border-stroke">
                      <p className="text-xs text-zinc-500 mb-1">Orçamento estimado</p>
                      <p className="text-sm font-mono text-zinc-200">
                        {formData.estimatedBudgetCents != null
                          ? formatBrlFromCents(formData.estimatedBudgetCents)
                          : '—'}
                      </p>
                    </div>
                  </div>

                  <div className="p-4 rounded-lg bg-paper border border-stroke">
                    <p className="text-xs text-zinc-500 mb-1">Contato</p>
                    <p className="text-sm text-zinc-200">
                      {formData.buyerContactName} — {formData.buyerContactPhone}
                    </p>
                  </div>
                </div>
              </div>

              <div className="flex gap-3">
                <Button variant="outline" size="lg" className="flex-1" onClick={() => setStep('edit')}>
                  Voltar e editar
                </Button>
                <Button size="lg" className="flex-1" onClick={() => void handleSubmit()} isLoading={loading}>
                  {loading ? (
                    <>
                      <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                      Publicando...
                    </>
                  ) : (
                    'Publicar solicitação'
                  )}
                </Button>
              </div>
            </div>
          )}

          {/* Info Box */}
          <div className="mt-6 p-4 rounded-lg bg-mint/5 border border-mint/20">
            <p className="text-sm text-zinc-400">
              <strong className="text-mint">Dica:</strong> Quanto mais detalhada for sua
              descrição, melhores serão as propostas que você receberá. Inclua especificações
              técnicas, requisitos de qualidade e condições especiais.
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
