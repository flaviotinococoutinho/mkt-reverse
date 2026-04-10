import { z } from 'zod'
import { httpJson } from '@/api/http'
import { unwrapHalPage } from '@/api/hal'

// Mirrors api-gateway JSON shape as used by tests.

export const SpecAttributeSchema = z
  .object({
    key: z.string(),
    type: z.string(),
    value: z.unknown(),
    unit: z.string().optional(),
  })
  .passthrough()

export const SourcingEventViewSchema = z
  .object({
    id: z.string(),
    tenantId: z.string().optional(),
    buyerOrganizationId: z.string().optional(),
    title: z.string(),
    description: z.string().optional(),
    status: z.string(),
    mccCategoryCode: z.number().optional(),
    productName: z.string().optional(),
    productDescription: z.string().optional(),
    quantityRequired: z.number().optional(),
    unitOfMeasure: z.string().optional(),
    attributes: z.array(SpecAttributeSchema).optional(),
    createdAtEpochMillis: z.number().optional(),
    _links: z.record(z.string(), z.object({ href: z.string() })).optional(),
  })
  .passthrough()

export type SourcingEventView = z.infer<typeof SourcingEventViewSchema>

export const SupplierResponseViewSchema = z
  .object({
    id: z.string(),
    supplierId: z.string(),
    status: z.string().optional(),
    offerCents: z.number().optional(),
    leadTimeDays: z.number().optional(),
    warrantyMonths: z.number().optional(),
    condition: z.string().optional(),
    shippingMode: z.string().optional(),
    message: z.string().optional(),
    attributes: z.array(SpecAttributeSchema).optional(),
    _links: z.record(z.string(), z.object({ href: z.string() })).optional(),
  })
  .passthrough()

export type SupplierResponseView = z.infer<typeof SupplierResponseViewSchema>

export type OpportunitiesQuery = {
  tenantId?: string
  supplierId: string
  mccCategoryCode?: number
  q?: string
  visibility?: 'OPEN' | 'INVITE_ONLY' | 'ALL'
  sortBy?: 'PUBLICATION_AT' | 'DEADLINE' | 'TITLE'
  sortDir?: 'ASC' | 'DESC'
  page?: number
  size?: number
}

function qs(params: Record<string, string | number | undefined>) {
  const sp = new URLSearchParams()
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === '') continue
    sp.set(k, String(v))
  }
  const s = sp.toString()
  return s ? `?${s}` : ''
}

export async function listOpportunities(q: OpportunitiesQuery) {
  const res = await httpJson<unknown>(
    `/api/v1/opportunities${qs({
      tenantId: q.tenantId,
      supplierId: q.supplierId,
      mccCategoryCode: q.mccCategoryCode,
      q: q.q,
      visibility: q.visibility ?? 'ALL',
      sortBy: q.sortBy ?? 'PUBLICATION_AT',
      sortDir: q.sortDir ?? 'DESC',
      page: q.page ?? 0,
      size: q.size ?? 20,
    })}`,
    { headers: { Accept: 'application/hal+json' } },
  )
  return unwrapHalPage(res, 'sourcingEventViewList', SourcingEventViewSchema)
}

export type BuyerEventsQuery = {
  tenantId?: string
  status?: string
  mccCategoryCode?: number
  page?: number
  size?: number
}

export async function listBuyerEvents(q: BuyerEventsQuery) {
  const res = await httpJson<unknown>(
    `/api/v1/sourcing-events${qs({
      tenantId: q.tenantId,
      status: q.status,
      mccCategoryCode: q.mccCategoryCode,
      page: q.page ?? 0,
      size: q.size ?? 20,
    })}`,
    { headers: { Accept: 'application/hal+json' } },
  )
  return unwrapHalPage(res, 'sourcingEventViewList', SourcingEventViewSchema)
}

export async function searchOpportunities(q: OpportunitiesQuery) {
  const res = await httpJson<unknown>(
    `/api/v1/opportunities/search${qs({
      tenantId: q.tenantId,
      supplierId: q.supplierId,
      mccCategoryCode: q.mccCategoryCode,
      q: q.q,
      visibility: q.visibility ?? 'ALL',
      sortBy: q.sortBy ?? 'PUBLICATION_AT',
      sortDir: q.sortDir ?? 'DESC',
      page: q.page ?? 0,
      size: q.size ?? 20,
    })}`,
    { headers: { Accept: 'application/hal+json' } },
  )
  return unwrapHalPage(res, 'sourcingEventViewList', SourcingEventViewSchema)
}

export type CreateEventRequest = {
  tenantId: string
  buyerOrganizationId: string
  buyerContactName: string
  buyerContactPhone: string
  title: string
  description?: string
  mccCategoryCode: number
  productName: string
  productDescription?: string
  category: string
  unitOfMeasure: string
  quantityRequired: number
  attributes: Array<{ key: string; type: string; value: unknown; unit?: string }>
  validForHours: number
  estimatedBudgetCents?: number
}

export async function createEvent(req: CreateEventRequest) {
  return httpJson<SourcingEventView>(`/api/v1/sourcing-events`, {
    method: 'POST',
    body: JSON.stringify(req),
  })
}

export async function getEvent(eventId: string) {
  const res = await httpJson<unknown>(`/api/v1/sourcing-events/${eventId}`)
  return SourcingEventViewSchema.parse(res)
}

export async function listResponses(eventId: string) {
  const res = await httpJson<unknown>(`/api/v1/sourcing-events/${eventId}/responses`)
  return z.array(SupplierResponseViewSchema).parse(res)
}

export type SubmitResponseRequest = {
  supplierId: string
  offerCents: number
  leadTimeDays: number
  warrantyMonths: number
  condition: string
  shippingMode: string
  attributes: Array<{ key: string; type: string; value: unknown; unit?: string }>
  message?: string
}

export async function submitResponse(eventId: string, req: SubmitResponseRequest) {
  return httpJson<SupplierResponseView>(`/api/v1/sourcing-events/${eventId}/responses`, {
    method: 'POST',
    body: JSON.stringify(req),
  })
}

export async function acceptResponse(eventId: string, responseId: string) {
  return httpJson<void>(
    `/api/v1/sourcing-events/${eventId}/responses/${responseId}/accept`,
    { method: 'POST' },
  )
}
