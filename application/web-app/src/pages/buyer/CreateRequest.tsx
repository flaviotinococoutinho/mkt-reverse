import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../context/useAuth';
import { AppHeader } from '../../components/layout/AppHeader';
import { sourcingService } from '../../services/sourcingService';
import { Loader2, ArrowLeft, ArrowRight, Send, Eye, Edit3 } from 'lucide-react';
import { useToast } from '../../components/ui/feedback';
import { getEventTypeLabel } from '../../lib/eventType';
import { formatBrlFromCents } from '../../lib/currency';
import { formatBrazilPhone } from '../../lib/phone';

const createRequestSchema = z.object({
  buyerContactPhone: z
    .string()
    .min(1, 'Telefone é obrigatório')
    .refine((val) => {
      const digits = val.replace(/\D/g, '');
      return digits.length >= 10 && digits.length <= 11;
    }, { message: 'Telefone inválido (10 ou 11 dígitos)' }),
  title: z.string().min(1, 'Título é obrigatório').min(3, 'Título deve ter pelo menos 3 caracteres'),
  description: z.string().min(1, 'Descrição é obrigatória').min(10, 'Descrição deve ter pelo menos 10 caracteres'),
  type: z.enum(['RFQ', 'REVERSE_AUCTION', 'MARKETPLACE']),
  productName: z.string().min(1, 'Nome do produto é obrigatório'),
  productDescription: z.string().optional(),
  category: z.string().optional(),
  unitOfMeasure: z.string().default('un'),
  quantityRequired: z.number().min(1, 'Quantidade deve ser pelo menos 1'),
  validForHours: z.number().min(1, 'Validade deve ser pelo menos 1 hora'),
});

type CreateRequestFormData = z.infer<typeof createRequestSchema>;

const EVENT_TYPES = [
  { value: 'RFQ', label: 'RFQ (Solicitação de Cotação)' },
  { value: 'REVERSE_AUCTION', label: 'Leilão Reverso' },
  { value: 'MARKETPLACE', label: 'Marketplace' },
];

const CATEGORIES = [
  { value: 'electronics', label: 'Eletrônicos' },
  { value: 'office', label: 'Escritório' },
  { value: 'furniture', label: 'Móveis' },
  { value: 'machinery', label: 'Maquinário' },
  { value: 'raw_materials', label: 'Matérias Primas' },
  { value: 'services', label: 'Serviços' },
  { value: 'other', label: 'Outros' },
];

const UNIT_OPTIONS = [
  { value: 'un', label: 'Unidade' },
  { value: 'kg', label: 'Kg' },
  { value: 'ton', label: 'Tonelada' },
  { value: 'l', label: 'Litro' },
  { value: 'm', label: 'Metro' },
  { value: 'm2', label: 'Metro Quadrado' },
  { value: 'm3', label: 'Metro Cúbico' },
  { value: 'h', label: 'Horas' },
];

export default function CreateRequest() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { error: showError, success } = useToast();

  const [step, setStep] = useState<'edit' | 'preview'>('edit');
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isValid },
  } = useForm<CreateRequestFormData>({
    resolver: zodResolver(createRequestSchema),
    mode: 'onChange',
    defaultValues: {
      buyerContactPhone: '',
      title: '',
      description: '',
      type: 'RFQ',
      productName: '',
      productDescription: '',
      category: '',
      unitOfMeasure: 'un',
      quantityRequired: 1,
      validForHours: 168,
    },
  });

  const formValues = watch();

  const onSubmit = async (data: CreateRequestFormData) => {
    setSubmitting(true);
    try {
      const requestData = {
        tenantId: user?.tenantId || 'tenant-default',
        buyerOrganizationId: user?.organizationId || '',
        buyerContactName: user?.name || '',
        buyerContactPhone: data.buyerContactPhone,
        title: data.title,
        description: data.description,
        type: data.type,
        productName: data.productName,
        productDescription: data.productDescription,
        category: data.category,
        unitOfMeasure: data.unitOfMeasure,
        quantityRequired: data.quantityRequired,
        validForHours: data.validForHours,
      };

      const response = await sourcingService.createSourcingEvent(requestData);
      success('Solicitação criada!', 'Sua solicitação foi publicada com sucesso.');
      navigate(`/sourcing-events/${response.id}`);
    } catch (err) {
      console.error('Failed to create request:', err);
      showError('Erro ao criar', 'Não foi possível criar a solicitação. Tente novamente.');
    } finally {
      setSubmitting(false);
    }
  };

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setValue('buyerContactPhone', formatBrazilPhone(e.target.value), { shouldValidate: true });
  };

  const handlePreview = () => {
    if (!isValid) {
      showError('Formulário inválido', 'Revise os campos obrigatórios antes de continuar.');
      return;
    }
    setStep('preview');
  };

  return (
    <div className="min-h-screen bg-ink text-zinc-200 font-sans">
      <AppHeader backTo="/buyer/dashboard" backLabel="Dashboard" />

      <main className="container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto">
          {/* Page Title */}
          <div className="mb-8 text-center">
            <h1 className="text-3xl font-serif text-zinc-100 mb-2">Publicar Solicitação</h1>
            <p className="text-zinc-400">
              Descreva o produto que você precisa e receba propostas de fornecedores
            </p>
          </div>

          {/* Step Indicator */}
          <div className="flex justify-center mb-8">
            <div className="flex items gap-4">
              <button
                type="button"
                onClick={() => setStep('edit')}
                className={`flex items-center gap-2 px-4 py-2 rounded-full transition-colors ${
                  step === 'edit' ? 'bg-citrus text-ink font-bold' : 'bg-zinc-800 text-zinc-400'
                }`}
              >
                <Edit3 className="w-4 h-4" />
                Editar
              </button>
              <div className="w-8 h-px bg-zinc-600" />
              <button
                type="button"
                onClick={handlePreview}
                disabled={!isValid}
                className={`flex items-center gap-2 px-4 py-2 rounded-full transition-colors ${
                  step === 'preview' ? 'bg-citrus text-ink font-bold' : 'bg-zinc-800 text-zinc-400'
                }`}
              >
                <Eye className="w-4 h-4" />
                Prévia
              </button>
            </div>
          </div>

          {step === 'edit' ? (
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {/* Tipo de Solicitação */}
              <div className="auction-panel p-4">
                <label className="block text-sm font-medium text-zinc-300 mb-3">
                  Tipo de Solicitação <span className="text-red-400">*</span>
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {EVENT_TYPES.map((type) => (
                    <label
                      key={type.value}
                      className={`
                        flex items-center justify-center p-3 rounded-lg border cursor-pointer text-center text-sm transition-colors
                        ${formValues.type === type.value 
                          ? 'border-citrus bg-citrus/10 text-zinc-100' 
                          : 'border-stroke hover:border-zinc-600 text-zinc-400'
                        }
                      `}
                    >
                      <input
                        type="radio"
                        {...register('type')}
                        value={type.value}
                        className="sr-only"
                      />
                      {type.label}
                    </label>
                  ))}
                </div>
              </div>

              {/* Informações do Produto */}
              <div className="auction-panel p-4 space-y-4">
                <h3 className="text-lg font-medium text-zinc-200">Informações do Produto</h3>
                
                <div>
                  <label className="block text-sm font-medium text-zinc-300 mb-1">
                    Título <span className="text-red-400">*</span>
                  </label>
                  <input
                    {...register('title')}
                    placeholder="Ex: Compra de notebooks Dell XPS 15"
                    className={`w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-citrus ${
                      errors.title ? 'border-red-500' : 'border-stroke'
                    }`}
                  />
                  {errors.title && <p className="text-sm text-red-400 mt-1">{errors.title.message}</p>}
                </div>

                <div>
                  <label className="block text-sm font-medium text-zinc-300 mb-1">
                    Descrição <span className="text-red-400">*</span>
                  </label>
                  <textarea
                    {...register('description')}
                    placeholder="Descreva detalhes, especificações técnicas, condições de entrega..."
                    rows={4}
                    className={`w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-citrus resize-y ${
                      errors.description ? 'border-red-500' : 'border-stroke'
                    }`}
                  />
                  {errors.description && <p className="text-sm text-red-400 mt-1">{errors.description.message}</p>}
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-zinc-300 mb-1">
                      Nome do Produto <span className="text-red-400">*</span>
                    </label>
                    <input
                      {...register('productName')}
                      placeholder="Ex: Notebook Dell XPS 15"
                      className={`w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-citrus ${
                        errors.productName ? 'border-red-500' : 'border-stroke'
                      }`}
                    />
                    {errors.productName && <p className="text-sm text-red-400 mt-1">{errors.productName.message}</p>}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-zinc-300 mb-1">Categoria</label>
                    <select
                      {...register('category')}
                      className="w-full px-3 py-2 bg-ink border border-stroke rounded-md text-zinc-200 focus:outline-none focus:ring-2 focus:ring-citrus"
                    >
                      <option value="">Selecione...</option>
                      {CATEGORIES.map((cat) => (
                        <option key={cat.value} value={cat.value}>{cat.label}</option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-zinc-300 mb-1">
                      Quantidade <span className="text-red-400">*</span>
                    </label>
                    <input
                      {...register('quantityRequired', { valueAsNumber: true })}
                      type="number"
                      min="1"
                      className={`w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 focus:outline-none focus:ring-2 focus:ring-citrus ${
                        errors.quantityRequired ? 'border-red-500' : 'border-stroke'
                      }`}
                    />
                    {errors.quantityRequired && <p className="text-sm text-red-400 mt-1">{errors.quantityRequired.message}</p>}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-zinc-300 mb-1">Unidade</label>
                    <select
                      {...register('unitOfMeasure')}
                      className="w-full px-3 py-2 bg-ink border border-stroke rounded-md text-zinc-200 focus:outline-none focus:ring-2 focus:ring-citrus"
                    >
                      {UNIT_OPTIONS.map((unit) => (
                        <option key={unit.value} value={unit.value}>{unit.label}</option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-zinc-300 mb-1">Validade (horas)</label>
                    <input
                      {...register('validForHours', { valueAsNumber: true })}
                      type="number"
                      min="1"
                      className="w-full px-3 py-2 bg-ink border border-stroke rounded-md text-zinc-200 focus:outline-none focus:ring-2 focus:ring-citrus"
                    />
                  </div>
                </div>
              </div>

              {/* Contato */}
              <div className="auction-panel p-4 space-y-4">
                <h3 className="text-lg font-medium text-zinc-200">Informações de Contato</h3>
                
                <div>
                  <label className="block text-sm font-medium text-zinc-300 mb-1">
                    Telefone / WhatsApp <span className="text-red-400">*</span>
                  </label>
                  <input
                    {...register('buyerContactPhone')}
                    placeholder="(11) 99999-9999"
                    onChange={handlePhoneChange}
                    className={`w-full px-3 py-2 bg-ink border rounded-md text-zinc-200 placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-citrus ${
                      errors.buyerContactPhone ? 'border-red-500' : 'border-stroke'
                    }`}
                  />
                  {errors.buyerContactPhone && <p className="text-sm text-red-400 mt-1">{errors.buyerContactPhone.message}</p>}
                </div>
              </div>

              {/* Actions */}
              <div className="flex justify-between">
                <Button type="button" variant="outline" onClick={() => navigate('/buyer/dashboard')}>
                  <ArrowLeft className="mr-2 h-4 w-4" />
                  Cancelar
                </Button>
                <div className="flex gap-3">
                  <Button type="button" variant="secondary" onClick={handlePreview}>
                    <Eye className="mr-2 h-4 w-4" />
                    Prévia
                  </Button>
                  <Button type="submit" isLoading={submitting} disabled={!isValid}>
                    <Send className="mr-2 h-4 w-4" />
                    Publicar
                  </Button>
                </div>
              </div>
            </form>
          ) : (
            /* Preview Step */
            <div className="space-y-6">
              <div className="auction-panel p-6">
                <h3 className="text-xl font-serif text-zinc-100 mb-4">{formValues.title}</h3>
                <p className="text-zinc-400 mb-4">{formValues.description}</p>
                
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="text-zinc-500">Tipo:</span>
                    <span className="text-zinc-200 ml-2">{getEventTypeLabel(formValues.type)}</span>
                  </div>
                  <div>
                    <span className="text-zinc-500">Produto:</span>
                    <span className="text-zinc-200 ml-2">{formValues.productName}</span>
                  </div>
                  <div>
                    <span className="text-zinc-500">Quantidade:</span>
                    <span className="text-zinc-200 ml-2">{formValues.quantityRequired} {formValues.unitOfMeasure}</span>
                  </div>
                  <div>
                    <span className="text-zinc-500">Validade:</span>
                    <span className="text-zinc-200 ml-2">{formValues.validForHours}h</span>
                  </div>
                </div>
              </div>

              <div className="flex justify-between">
                <Button variant="outline" onClick={() => setStep('edit')}>
                  <ArrowLeft className="mr-2 h-4 w-4" />
                  Voltar
                </Button>
                <Button onClick={handleSubmit(onSubmit)} isLoading={submitting}>
                  <Send className="mr-2 h-4 w-4" />
                  Confirmar Publicação
                </Button>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}