import { correlationId } from '@/lib/ids'

export type ProblemDetails = {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  code?: string
  correlationId?: string
}

export class ApiError extends Error {
  readonly status: number
  readonly problem?: ProblemDetails
  readonly correlationId?: string

  constructor(message: string, status: number, problem?: ProblemDetails) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
    this.correlationId = problem?.correlationId
  }
}

export async function httpJson<T>(
  url: string,
  init: RequestInit = {},
): Promise<T> {
  const headers = new Headers(init.headers)
  if (!headers.has('Content-Type') && init.body) {
    headers.set('Content-Type', 'application/json')
  }
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json')
  }
  if (!headers.has('X-Correlation-Id')) {
    headers.set('X-Correlation-Id', correlationId())
  }

  const res = await fetch(url, { ...init, headers })
  const correlation = res.headers.get('X-Correlation-Id') ?? undefined

  if (res.status === 204) {
    return undefined as T
  }

  const contentType = res.headers.get('content-type') ?? ''
  const isJson = contentType.includes('application/json') || contentType.includes('+json')
  const body = isJson ? await res.json().catch(() => undefined) : await res.text().catch(() => '')

  if (!res.ok) {
    const problem = typeof body === 'object' && body ? (body as ProblemDetails) : undefined
    const code = problem?.code ? ` (${problem.code})` : ''
    const msg = `HTTP ${res.status}${code}`
    const err = new ApiError(msg, res.status, problem)
    ;(err as any).correlationId = problem?.correlationId ?? correlation
    throw err
  }

  return body as T
}

