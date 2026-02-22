import React from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'

import {
  acceptResponse,
  getEvent,
  listResponses,
  submitResponse,
  type SubmitResponseRequest,
} from '@/api/sourcing'
import { useAppSettings } from '@/ui/SettingsDock'
import { AttributeEditor, SpecAttribute } from '@/ui/components/AttributeEditor'
import { useAuth } from '@/ui/context/AuthContext'

export function EventDetailPage() {
  const { eventId } = useParams()
  if (!eventId) throw new Error('Missing eventId')

  const qc = useQueryClient()
  const { supplierId: settingsSupplierId } = useAppSettings()
  const { user } = useAuth()

  // Determine effective supplierId: Auth context takes precedence, fallback to SettingsDock
  // In a real app, 'user.tenantId' or specific 'user.supplierId' would be used.
  const effectiveSupplierId = user ? user.tenantId : settingsSupplierId

  const eventQ = useQuery({
    queryKey: ['event', eventId],
    queryFn: () => getEvent(eventId),
  })
  const responsesQ = useQuery({
    queryKey: ['responses', eventId],
    queryFn: () => listResponses(eventId),
  })

  const [offer, setOffer] = React.useState<SubmitResponseRequest>({
    supplierId: effectiveSupplierId,
    offerCents: 19900,
    leadTimeDays: 3,
    warrantyMonths: 3,
    condition: 'USED',
    shippingMode: 'PICKUP',
    message: 'Tenho em estoque',
    attributes: [{ key: 'voltage', type: 'VOLTAGE', unit: 'V', value: 220 }],
  })

  // Keep supplierId in sync if context/settings change
  React.useEffect(() => {
    setOffer((o) => ({ ...o, supplierId: effectiveSupplierId }))
  }, [effectiveSupplierId])

  const submitM = useMutation({
    mutationFn: () => submitResponse(eventId, offer),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ['responses', eventId] })
    },
  })

  const acceptM = useMutation({
    mutationFn: ({ responseId }: { responseId: string }) =>
      acceptResponse(eventId, responseId),
    onSuccess: async () => {
      await Promise.all([
        qc.invalidateQueries({ queryKey: ['responses', eventId] }),
        qc.invalidateQueries({ queryKey: ['event', eventId] }),
      ])
    },
  })

  const handleAttributesChange = (newAttrs: SpecAttribute[]) => {
    setOffer((prev) => ({ ...prev, attributes: newAttrs }))
  }

  return (
    <section className="page">
      <div className="pageHeader">
        <div className="crumbs">
          <Link className="crumb" to="/">
            Oportunidades
          </Link>
          <span className="dot" aria-hidden />
          <span className="mono">event</span>
        </div>

        {eventQ.data ? (
          <div className="eventHeader">
            <h1 className="h1">{eventQ.data.title}</h1>
            <div className="pill big">{eventQ.data.status}</div>
          </div>
        ) : (
          <h1 className="h1">Carregando…</h1>
        )}
      </div>

      <div className="threeCol">
        <div className="panel">
          <div className="panelTitle">Detalhes</div>
          {eventQ.data && (
            <div className="kv">
              <div className="k">id</div>
              <div className="v mono">{eventQ.data.id}</div>
              <div className="k">mcc</div>
              <div className="v">{eventQ.data.mccCategoryCode ?? '—'}</div>
              <div className="k">produto</div>
              <div className="v">{eventQ.data.productName ?? '—'}</div>
              <div className="k">qtd</div>
              <div className="v">
                {eventQ.data.quantityRequired ?? '—'} {eventQ.data.unitOfMeasure ?? ''}
              </div>
              <div className="k">descrição</div>
              <div className="v">{eventQ.data.description ?? '—'}</div>
              
              {eventQ.data.attributes && eventQ.data.attributes.length > 0 && (
                <>
                  <div className="k" style={{marginTop: '0.5rem'}}>atributos exigidos</div>
                  <div className="v">
                    <ul style={{ paddingLeft: '1rem', margin: 0 }}>
                      {eventQ.data.attributes.map((attr: any, i: number) => (
                        <li key={i} className="mono small">
                          {attr.key}: {String(attr.value)} {attr.unit}
                        </li>
                      ))}
                    </ul>
                  </div>
                </>
              )}
            </div>
          )}
        </div>

        <div className="panel">
          <div className="panelTitle">Propostas</div>
          {responsesQ.isLoading && <div className="skeleton">Carregando…</div>}
          {responsesQ.data && (
            <div className="stack">
              {responsesQ.data.length === 0 && (
                <div className="muted">Nenhuma proposta ainda.</div>
              )}

              {responsesQ.data.map((r) => (
                <div className="row" key={r.id}>
                  <div className="rowTop">
                    <div className="rowTitle">
                      <span className="mono">supplier</span> {r.supplierId}
                    </div>
                    <div className="pill">{r.status ?? '—'}</div>
                  </div>
                  <div className="rowBody">
                    <div>
                      <span className="mono">offer</span> {r.offerCents ?? '—'}
                    </div>
                    <div>
                      <span className="mono">lead</span> {r.leadTimeDays ?? '—'}d
                    </div>
                    <div>
                      <span className="mono">warranty</span> {r.warrantyMonths ?? '—'}m
                    </div>
                    {r.attributes && r.attributes.length > 0 && (
                      <div style={{ marginTop: '0.5rem', fontSize: '0.8rem', color: '#666' }}>
                        {r.attributes.map(a => `${a.key}=${a.value}`).join(', ')}
                      </div>
                    )}
                  </div>
                  <div className="rowActions">
                    <button
                      className="btn"
                      disabled={acceptM.isPending || eventQ.data?.status !== 'PUBLISHED' && eventQ.data?.status !== 'IN_PROGRESS' && eventQ.data?.status !== 'NEGOTIATION' && eventQ.data?.status !== 'UNDER_EVALUATION'}
                      onClick={() => acceptM.mutate({ responseId: r.id })}
                      title="Aceitar proposta (gera contrato)"
                    >
                      Aceitar
                    </button>
                    <span className="mono small">{r.id}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="panel">
          <div className="panelTitle">Enviar proposta</div>
          <form
            className="form"
            onSubmit={(e) => {
              e.preventDefault()
              submitM.mutate()
            }}
          >
            <label className="field">
              <div className="label">supplierId</div>
              <input
                className="input"
                value={offer.supplierId}
                onChange={(e) =>
                  setOffer((s) => ({ ...s, supplierId: e.target.value }))
                }
                disabled={!!user} // Disable if logged in via AuthContext
              />
            </label>

            <div className="twoCol">
              <label className="field">
                <div className="label">offerCents</div>
                <input
                  className="input"
                  inputMode="numeric"
                  value={offer.offerCents}
                  onChange={(e) =>
                    setOffer((s) => ({ ...s, offerCents: Number(e.target.value) }))
                  }
                />
              </label>
              <label className="field">
                <div className="label">leadTimeDays</div>
                <input
                  className="input"
                  inputMode="numeric"
                  value={offer.leadTimeDays}
                  onChange={(e) =>
                    setOffer((s) => ({ ...s, leadTimeDays: Number(e.target.value) }))
                  }
                />
              </label>
            </div>

            <div className="twoCol">
              <label className="field">
                <div className="label">warrantyMonths</div>
                <input
                  className="input"
                  inputMode="numeric"
                  value={offer.warrantyMonths}
                  onChange={(e) =>
                    setOffer((s) => ({ ...s, warrantyMonths: Number(e.target.value) }))
                  }
                />
              </label>
              <label className="field">
                <div className="label">condition</div>
                <select
                  className="select"
                  value={offer.condition}
                  onChange={(e) =>
                    setOffer((s) => ({ ...s, condition: e.target.value }))
                  }
                >
                  <option value="NEW">Novo</option>
                  <option value="USED">Usado</option>
                  <option value="REFURBISHED">Recondicionado</option>
                  <option value="FOR_PARTS">Para peças</option>
                </select>
              </label>
            </div>

            <div className="twoCol">
              <label className="field">
                <div className="label">shippingMode</div>
                <select
                  className="select"
                  value={offer.shippingMode}
                  onChange={(e) =>
                    setOffer((s) => ({ ...s, shippingMode: e.target.value }))
                  }
                >
                  <option value="PICKUP">PICKUP</option>
                  <option value="DELIVERY">DELIVERY</option>
                </select>
              </label>
              <div className="field" /> {/* Spacer */}
            </div>

            <label className="field">
              <div className="label">message</div>
              <textarea
                className="textarea"
                rows={2}
                value={offer.message ?? ''}
                onChange={(e) =>
                  setOffer((s) => ({ ...s, message: e.target.value }))
                }
              />
            </label>

            {/* Integrated AttributeEditor for Supplier Response */}
            <div className="field">
              <div className="label">Atributos Técnicos (Especificação)</div>
              <div style={{ marginTop: '0.25rem', border: '1px solid #eee', borderRadius: '8px', padding: '0.5rem', background: '#fafafa' }}>
                <AttributeEditor
                  attributes={offer.attributes}
                  onChange={handleAttributesChange}
                />
              </div>
            </div>

            <div className="actions">
              <button className="btn" type="submit" disabled={submitM.isPending}>
                {submitM.isPending ? 'Enviando…' : 'Enviar Proposta'}
              </button>
              {submitM.isError && (
                <div className="errorInline">{String((submitM.error as any)?.message)}</div>
              )}
            </div>
          </form>
        </div>
      </div>
    </section>
  )
}
