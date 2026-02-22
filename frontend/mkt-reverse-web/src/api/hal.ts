import { z } from 'zod'

export const HalLinkSchema = z.object({ href: z.string() }).passthrough()

export const HalLinksSchema = z
  .record(z.string(), HalLinkSchema)
  .optional()

export const PageMetaSchema = z.object({
  size: z.number(),
  totalElements: z.number(),
  totalPages: z.number(),
  number: z.number(),
})

export function halListSchema<T extends z.ZodTypeAny>(
  embeddedKey: string,
  itemSchema: T,
) {
  return z.object({
    _embedded: z.object({
      [embeddedKey]: z.array(itemSchema),
    }),
    _links: HalLinksSchema,
    page: PageMetaSchema,
  })
}

export type HalPage<T> = {
  items: T[]
  page: z.infer<typeof PageMetaSchema>
}

export function unwrapHalPage<T>(
  hal: unknown,
  embeddedKey: string,
  itemSchema: z.ZodType<T>,
): HalPage<T> {
  const parsed = halListSchema(embeddedKey, itemSchema).parse(hal)
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const items = (parsed._embedded as any)[embeddedKey] as T[]
  return { items, page: parsed.page }
}

