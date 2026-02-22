import api from './api';

export interface SpecAttribute {
  name: string;
  value: string;
}

export interface CreateSourcingEventRequest {
  tenantId: string;
  buyerOrganizationId: string;
  buyerContactName: string;
  buyerContactPhone: string;
  title: string;
  description?: string;
  type?: 'RFQ' | 'REVERSE_AUCTION';
  mccCategoryCode?: number;
  productName: string;
  productDescription?: string;
  category?: string;
  unitOfMeasure: string;
  quantityRequired: number;
  attributes?: SpecAttribute[];
  validForHours: number;
  estimatedBudgetCents?: number;
}

export interface CreateSourcingEventResponse {
  id: string;
}

export interface UpdateSourcingEventRequest {
  tenantId?: string;
  title: string;
  description?: string;
}

export interface SourcingEventView {
  id: string;
  status: string;
  title: string;
  description?: string;
  eventType: string;
  tenantId: string;
  buyerOrganizationId: string;
  awardedSupplierId?: string;
}

export interface SupplierResponseView {
  id: string;
  eventId: string;
  supplierId: string;
  status: string;
  offerCents: number;
  currency: string;
  leadTimeDays?: number;
  warrantyMonths?: number;
  condition?: string;
  shippingMode?: string;
  attributes?: SpecAttribute[];
  message?: string;
}

export interface SupplierResponseRequest {
  supplierId: string;
  offerCents: number;
  leadTimeDays?: number;
  warrantyMonths?: number;
  condition?: 'NEW' | 'USED' | 'REFURBISHED' | 'FOR_PARTS' | 'UNKNOWN';
  shippingMode?: 'PICKUP' | 'DELIVERY' | 'SHIPPING' | 'DIGITAL' | 'UNKNOWN';
  attributes?: SpecAttribute[];
  message?: string;
}

const EMBEDDED_CANDIDATE_KEYS = [
  'entityModelList',
  'sourcingEventViewList',
  'sourcingEventViews',
  'items',
] as const;

function extractEmbeddedList<T>(payload: unknown): T[] {
  if (!payload || typeof payload !== 'object') {
    return [];
  }

  const embedded = (payload as { _embedded?: Record<string, unknown> })._embedded;
  if (!embedded || typeof embedded !== 'object') {
    return [];
  }

  for (const key of EMBEDDED_CANDIDATE_KEYS) {
    const candidate = embedded[key];
    if (Array.isArray(candidate)) {
      return candidate as T[];
    }
  }

  const firstArray = Object.values(embedded).find(Array.isArray);
  return Array.isArray(firstArray) ? (firstArray as T[]) : [];
}

function normalizeSourcingEventView(payload: unknown, requestedId: string): SourcingEventView {
  if (!payload || typeof payload !== 'object') {
    return {
      id: requestedId,
      status: 'UNKNOWN',
      title: '',
      description: undefined,
      eventType: 'UNKNOWN',
      tenantId: '',
      buyerOrganizationId: '',
      awardedSupplierId: undefined,
    };
  }

  const candidate = payload as Partial<SourcingEventView>;

  return {
    id: typeof candidate.id === 'string' && candidate.id.length > 0 ? candidate.id : requestedId,
    status: typeof candidate.status === 'string' && candidate.status.length > 0 ? candidate.status : 'UNKNOWN',
    title: typeof candidate.title === 'string' ? candidate.title : '',
    description: typeof candidate.description === 'string' ? candidate.description : undefined,
    eventType:
      typeof candidate.eventType === 'string' && candidate.eventType.length > 0 ? candidate.eventType : 'UNKNOWN',
    tenantId: typeof candidate.tenantId === 'string' ? candidate.tenantId : '',
    buyerOrganizationId:
      typeof candidate.buyerOrganizationId === 'string' ? candidate.buyerOrganizationId : '',
    awardedSupplierId:
      typeof candidate.awardedSupplierId === 'string' ? candidate.awardedSupplierId : undefined,
  };
}

export const sourcingService = {
  async createSourcingEvent(data: CreateSourcingEventRequest): Promise<CreateSourcingEventResponse> {
    // api baseURL already points to /api/v1
    const response = await api.post('/sourcing-events', data);
    return response.data;
  },

  async getSourcingEvents(params?: {
    tenantId?: string;
    status?: string;
    mccCategoryCode?: number;
    page?: number;
    size?: number;
  }): Promise<{ items: SourcingEventView[]; total: number }> {
    const response = await api.get('/sourcing-events', { params });
    const items = extractEmbeddedList<SourcingEventView>(response.data);
    return {
      items,
      total: response.data.page?.totalElements || items.length,
    };
  },

  async getSourcingEvent(id: string): Promise<SourcingEventView> {
    const response = await api.get(`/sourcing-events/${id}`);
    return normalizeSourcingEventView(response.data, id);
  },

  async updateSourcingEvent(id: string, data: UpdateSourcingEventRequest): Promise<void> {
    await api.patch(`/sourcing-events/${id}`, data);
  },

  async getResponses(eventId: string): Promise<SupplierResponseView[]> {
    const response = await api.get(`/sourcing-events/${eventId}/responses`);
    return Array.isArray(response.data) ? response.data : [];
  },

  async submitResponse(eventId: string, data: SupplierResponseRequest): Promise<{ id: string }> {
    const response = await api.post(`/sourcing-events/${eventId}/responses`, data);
    return response.data;
  },

  async acceptResponse(eventId: string, responseId: string): Promise<void> {
    await api.post(`/sourcing-events/${eventId}/responses/${responseId}/accept`);
  },

  async getOpportunities(params?: {
    tenantId?: string;
    supplierId?: string;
    mccCategoryCode?: number;
    q?: string;
    visibility?: string;
    sortBy?: string;
    sortDir?: string;
    page?: number;
    size?: number;
  }): Promise<{ items: SourcingEventView[]; total: number }> {
    const response = await api.get('/opportunities', { params });
    const items = extractEmbeddedList<SourcingEventView>(response.data);
    return {
      items,
      total: response.data.page?.totalElements || items.length,
    };
  },
};
