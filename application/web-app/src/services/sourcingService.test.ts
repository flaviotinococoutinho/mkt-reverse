import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('./api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
  },
}));

import api from './api';
import { sourcingService } from './sourcingService';

describe('sourcingService.createSourcingEvent', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('envia o payload esperado para POST /sourcing-events', async () => {
    const payload = {
      tenantId: 'tenant-1',
      buyerOrganizationId: 'buyer-org-1',
      buyerContactName: 'Flavio',
      buyerContactPhone: '(11) 99999-9999',
      title: 'Cotação notebooks',
      description: 'Preciso de 10 notebooks para time comercial',
      type: 'RFQ' as const,
      mccCategoryCode: 5732,
      productName: 'Notebook Dell',
      productDescription: 'i7, 16GB, SSD 512GB',
      category: 'Informática',
      unitOfMeasure: 'un',
      quantityRequired: 10,
      attributes: [
        { name: 'ram', value: '16GB' },
        { name: 'storage', value: '512GB SSD' },
      ],
      validForHours: 168,
      estimatedBudgetCents: 850000,
    };

    vi.mocked(api.post).mockResolvedValue({
      data: { id: 'evt-123' },
    });

    const result = await sourcingService.createSourcingEvent(payload);

    expect(api.post).toHaveBeenCalledWith('/sourcing-events', payload);
    expect(result).toEqual({ id: 'evt-123' });
  });
});

describe('sourcingService.getSourcingEvents', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('extrai lista HAL via _embedded.sourcingEventViewList e total via page.totalElements', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: {
        _embedded: {
          sourcingEventViewList: [
            { id: 'evt-1', title: 'A', status: 'PUBLISHED' },
            { id: 'evt-2', title: 'B', status: 'PUBLISHED' },
          ],
        },
        page: {
          totalElements: 5,
        },
      },
    });

    const result = await sourcingService.getSourcingEvents({ tenantId: 'tenant-1', page: 0, size: 20 });

    expect(api.get).toHaveBeenCalledWith('/sourcing-events', {
      params: { tenantId: 'tenant-1', page: 0, size: 20 },
    });
    expect(result.items).toHaveLength(2);
    expect(result.total).toBe(5);
  });
});

describe('sourcingService.getOpportunities', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('envia filtros de descoberta e usa fallback de total quando page.totalElements não existe', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: {
        _embedded: {
          items: [
            { id: 'evt-10', title: 'Notebook corporativo', status: 'PUBLISHED' },
            { id: 'evt-11', title: 'Desktop all-in-one', status: 'PUBLISHED' },
          ],
        },
      },
    });

    const result = await sourcingService.getOpportunities({
      tenantId: 'tenant-1',
      supplierId: 'supplier-77',
      q: 'notebook',
      sortBy: 'createdAt',
      sortDir: 'desc',
      page: 1,
      size: 10,
    });

    expect(api.get).toHaveBeenCalledWith('/opportunities', {
      params: {
        tenantId: 'tenant-1',
        supplierId: 'supplier-77',
        q: 'notebook',
        sortBy: 'createdAt',
        sortDir: 'desc',
        page: 1,
        size: 10,
      },
    });
    expect(result.items).toHaveLength(2);
    expect(result.total).toBe(2);
  });
});

describe('sourcingService.getSourcingEvent', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('retorna o detalhe do evento quando backend devolve payload válido', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: {
        id: 'evt-9',
        status: 'PUBLISHED',
        title: 'Compra de notebooks',
        description: 'Evento aberto para propostas',
        eventType: 'RFQ',
        tenantId: 'tenant-1',
        buyerOrganizationId: 'buyer-1',
      },
    });

    const result = await sourcingService.getSourcingEvent('evt-9');

    expect(api.get).toHaveBeenCalledWith('/sourcing-events/evt-9');
    expect(result).toMatchObject({
      id: 'evt-9',
      status: 'PUBLISHED',
      title: 'Compra de notebooks',
      eventType: 'RFQ',
      tenantId: 'tenant-1',
      buyerOrganizationId: 'buyer-1',
    });
  });

  it('normaliza fallback seguro quando backend devolve payload inválido/parcial', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: {
        status: null,
        title: null,
        eventType: null,
      },
    });

    const result = await sourcingService.getSourcingEvent('evt-fallback');

    expect(api.get).toHaveBeenCalledWith('/sourcing-events/evt-fallback');
    expect(result).toEqual({
      id: 'evt-fallback',
      status: 'UNKNOWN',
      title: '',
      description: undefined,
      eventType: 'UNKNOWN',
      tenantId: '',
      buyerOrganizationId: '',
      awardedSupplierId: undefined,
    });
  });
});

describe('sourcingService.getResponses', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('retorna lista de respostas quando backend devolve array', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: [
        {
          id: 'resp-1',
          eventId: 'evt-1',
          supplierId: 'supplier-1',
          status: 'SUBMITTED',
          offerCents: 120000,
          currency: 'BRL',
        },
      ],
    });

    const result = await sourcingService.getResponses('evt-1');

    expect(api.get).toHaveBeenCalledWith('/sourcing-events/evt-1/responses');
    expect(result).toHaveLength(1);
    expect(result[0]?.id).toBe('resp-1');
  });

  it('faz fallback seguro para lista vazia quando backend devolve payload inválido', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: {
        _embedded: {
          items: [
            {
              id: 'resp-x',
              eventId: 'evt-x',
              supplierId: 'supplier-x',
            },
          ],
        },
      },
    });

    const result = await sourcingService.getResponses('evt-x');

    expect(api.get).toHaveBeenCalledWith('/sourcing-events/evt-x/responses');
    expect(result).toEqual([]);
  });
});

describe('sourcingService.updateSourcingEvent', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('envia PATCH /sourcing-events/:id com payload de atualização', async () => {
    vi.mocked(api.patch).mockResolvedValue({ data: undefined });

    await sourcingService.updateSourcingEvent('evt-55', {
      tenantId: 'tenant-1',
      title: 'Título atualizado',
      description: 'Nova descrição',
    });

    expect(api.patch).toHaveBeenCalledWith('/sourcing-events/evt-55', {
      tenantId: 'tenant-1',
      title: 'Título atualizado',
      description: 'Nova descrição',
    });
  });
});
