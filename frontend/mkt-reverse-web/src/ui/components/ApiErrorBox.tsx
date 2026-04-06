import React from 'react'

import { ApiError } from '@/api/http'

export function ApiErrorBox({ error, title }: { error: unknown; title?: string }) {
  const e = error as ApiError
  return (
    <div className="error" role="alert" aria-live="assertive">
      <div className="errorTitle">{title ?? 'Falha na chamada'}</div>
      <div className="errorBody">
        <div>
          <span className="mono">message</span> {String(e.message)}
        </div>
        {e.problem?.code && (
          <div>
            <span className="mono">code</span> {e.problem.code}
          </div>
        )}
        {(e.problem?.correlationId || e.correlationId) && (
          <div>
            <span className="mono">correlationId</span> {e.problem?.correlationId ?? e.correlationId}
          </div>
        )}
      </div>
    </div>
  )
}

