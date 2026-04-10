export function correlationId(): string {
  // Prefer crypto.randomUUID when available.
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return (crypto as any).randomUUID()
  }
  // Fallback (good enough for local dev).
  return `cid_${Date.now()}_${Math.random().toString(16).slice(2)}`
}

