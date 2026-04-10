import { describe, it, expect } from 'vitest'
import { unwrapHalPage } from './hal'
import { z } from 'zod'

describe('HAL parsing', () => {
  it('unwraps paged HAL list with embedded key', () => {
    const payload = {
      _embedded: {
        sourcingEventViewList: [{ id: '1', title: 'A', status: 'OPEN' }],
      },
      _links: { self: { href: '/api/v1/opportunities' } },
      page: { size: 10, totalElements: 1, totalPages: 1, number: 0 },
    }

    const item = z.object({ id: z.string(), title: z.string(), status: z.string() })
    const out = unwrapHalPage(payload, 'sourcingEventViewList', item)

    expect(out.items).toHaveLength(1)
    expect(out.items[0].id).toBe('1')
    expect(out.page.totalElements).toBe(1)
  })
})

