import React from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'

import { listBuyerEvents } from '@/api/sourcing'
import { useAppSettings } from '@/ui/SettingsDock'
import { ApiErrorBox } from '@/ui/components/ApiErrorBox'
import { Page, Hero } from '@/ui/components/Page'
import { Panel, PanelTitle, PanelBody } from '@/ui/components/Panel'
import { Field, Select, TextInput } from '@/ui/components/FormFields'
import { Pager } from '@/ui/components/Pager'

export function BuyerEventsPage() {
  const { tenantId } = useAppSettings()
  const [page, setPage] = React.useState(0)
  const [status, setStatus] = React.useState<string>('')
  const [mccCategoryCode, setMccCategoryCode] = React.useState<string>('')

  const query = useQuery({
    queryKey: ['buyer-events', tenantId, status, mccCategoryCode, page],
    queryFn: async () =>
      listBuyerEvents({
        tenantId,
        status: status || undefined,
        mccCategoryCode: mccCategoryCode ? Number(mccCategoryCode) : undefined,
        page,
        size: 12,
      }),
  })

  return (
    <Page>
      <Hero>
        <div>
          <h1 className="h1">
            Eventos do <span className="h1Accent">Buyer</span>
          </h1>
          <p className="lede">
            Listagem paginada do lado comprador (sourcing-events). Essa tela é a base para gestão
            (editar, encerrar, convidar fornecedores, acompanhar propostas).
          </p>
          <div className="contextLine">
            <span className="pill">buyer lens</span>
            <span className="dot" aria-hidden="true" />
            <span className="mono">tenant</span> {tenantId}
          </div>
        </div>
        <div className="badge">
          <div className="badgeTop">PAGE SIZE</div>
          <div className="badgeBig">12</div>
          <div className="badgeBottom">itens por página</div>
        </div>
      </Hero>

      <Panel>
        <PanelTitle>Filtros</PanelTitle>
        <PanelBody>
          <div className="filters">
            <Field label="Status">
              <Select
                value={status}
                onChange={(e) => {
                  setStatus(e.target.value)
                  setPage(0)
                }}
              >
                <option value="">Todos</option>
                <option value="DRAFT">DRAFT</option>
                <option value="PUBLISHED">PUBLISHED</option>
                <option value="IN_PROGRESS">IN_PROGRESS</option>
                <option value="AWARDED">AWARDED</option>
                <option value="CLOSED">CLOSED</option>
                <option value="ARCHIVED">ARCHIVED</option>
              </Select>
            </Field>

            <Field label="MCC">
              <TextInput
                placeholder="ex.: 5045"
                value={mccCategoryCode}
                onChange={(e) => {
                  setMccCategoryCode(e.target.value)
                  setPage(0)
                }}
              />
            </Field>

            <div className="field">
              <div className="label">Ação</div>
              <button
                className="btn ghost"
                onClick={() => {
                  setStatus('')
                  setMccCategoryCode('')
                  setPage(0)
                }}
              >
                Limpar
              </button>
            </div>
          </div>
        </PanelBody>
      </Panel>

      <Panel>
        <PanelTitle>Resultados</PanelTitle>
        <PanelBody>
          {query.isLoading && <div className="skeleton">Consultando…</div>}

          {query.isError && <ApiErrorBox error={query.error} />}

          {query.data && (
            <>
              <div className="grid">
                {query.data.items.map((o) => (
                  <Link className="card" to={`/events/${o.id}`} key={o.id}>
                    <div className="cardTop">
                      <div className="cardTitle">{o.title}</div>
                      <div className="pill">{o.status}</div>
                    </div>
                    <div className="cardMeta">
                      <div className="mono">Tenant</div>
                      <div>{o.tenantId ?? '—'}</div>
                      <div className="mono">Buyer org</div>
                      <div>{o.buyerOrganizationId ?? '—'}</div>
                    </div>
                    <div className="cardFoot">
                      <span className="mono">id</span>
                      <span className="cardId">{o.id}</span>
                    </div>
                  </Link>
                ))}
              </div>

              <Pager
                page={page}
                totalPages={query.data.page.totalPages}
                onPrev={() => setPage((p) => Math.max(0, p - 1))}
                onNext={() => setPage((p) => p + 1)}
              />
            </>
          )}
        </PanelBody>
      </Panel>
    </Page>
  )
}
