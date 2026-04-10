import React from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

import { listOpportunities, searchOpportunities } from '@/api/sourcing'
import { useAppSettings } from '@/ui/SettingsDock'
import { ApiErrorBox } from '@/ui/components/ApiErrorBox'
import { Page, Hero, HeroLeft, HeroRight } from '@/ui/components/Page'
import { Panel, PanelTop, PanelBody } from '@/ui/components/Panel'
import { Field, Select, TextInput } from '@/ui/components/FormFields'
import { Pager } from '@/ui/components/Pager'

type Mode = 'DIRECTORY' | 'FUZZY'

export function OpportunitiesPage() {
  const { tenantId, supplierId } = useAppSettings()
  const [q, setQ] = React.useState('')
  const [visibility, setVisibility] = React.useState<'ALL' | 'OPEN' | 'INVITE_ONLY'>('ALL')
  const [mode, setMode] = React.useState<Mode>('DIRECTORY')
  const [mcc, setMcc] = React.useState('')
  const [sortBy, setSortBy] = React.useState<'PUBLICATION_AT' | 'DEADLINE' | 'TITLE'>('PUBLICATION_AT')
  const [sortDir, setSortDir] = React.useState<'ASC' | 'DESC'>('DESC')
  const [page, setPage] = React.useState(0)

  const queryFn = async () => {
    const base = {
      tenantId,
      supplierId,
      q,
      visibility,
      mccCategoryCode: mcc ? Number(mcc) : undefined,
      sortBy,
      sortDir,
      page,
      size: 12,
    }
    return mode === 'FUZZY' ? searchOpportunities(base) : listOpportunities(base)
  }

  const query = useQuery({
    queryKey: ['opportunities', mode, tenantId, supplierId, q, visibility, mcc, sortBy, sortDir, page],
    queryFn,
  })

  return (
    <Page>
      <Hero>
        <HeroLeft>
          <h1 className="h1">
            Oportunidades
            <span className="h1Accent"> para fornecedores</span>
          </h1>
          <p className="lede">
            A UI não mantém “estado de marketplace”. Ela só consulta o backend e executa ações.
          </p>
        </HeroLeft>
        <HeroRight>
          <div className="badge">
            <div className="badgeTop">source of truth</div>
            <div className="badgeBig">API</div>
            <div className="badgeBottom">HAL + ProblemDetails + Correlation-Id</div>
          </div>
        </HeroRight>
      </Hero>

      <Panel>
        <PanelTop>
          <div className="filters">
            <Field label="busca">
              <TextInput
                value={q}
                placeholder="ex.: pneu 195/55"
                onChange={(e) => {
                  setQ(e.target.value)
                  setPage(0)
                }}
              />
            </Field>

            <Field label="modo">
              <Select
                value={mode}
                onChange={(e) => {
                  setMode(e.target.value as Mode)
                  setPage(0)
                }}
              >
                <option value="DIRECTORY">Directory (Postgres)</option>
                <option value="FUZZY">Fuzzy (OpenSearch → fallback)</option>
              </Select>
            </Field>

            <Field label="mcc">
              <TextInput
                value={mcc}
                placeholder="ex.: 5045"
                onChange={(e) => {
                  setMcc(e.target.value)
                  setPage(0)
                }}
              />
            </Field>

            <Field label="visibilidade">
              <Select
                value={visibility}
                onChange={(e) => {
                  setVisibility(e.target.value as any)
                  setPage(0)
                }}
              >
                <option value="ALL">ALL</option>
                <option value="OPEN">OPEN</option>
                <option value="INVITE_ONLY">INVITE_ONLY</option>
              </Select>
            </Field>

            <Field label="ordenação">
              <Select
                value={`${sortBy}:${sortDir}`}
                onChange={(e) => {
                  const [by, dir] = e.target.value.split(':')
                  setSortBy(by as any)
                  setSortDir(dir as any)
                  setPage(0)
                }}
              >
                <option value="PUBLICATION_AT:DESC">Mais recentes</option>
                <option value="DEADLINE:ASC">Deadline mais próxima</option>
                <option value="TITLE:ASC">Título (A→Z)</option>
              </Select>
            </Field>
          </div>

          <div className="contextLine">
            <span className="mono">tenant</span> {tenantId} <span className="dot" aria-hidden />{' '}
            <span className="mono">supplier</span> {supplierId}
          </div>
        </PanelTop>

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
                      <div className="mono">MCC</div>
                      <div>{o.mccCategoryCode ?? '—'}</div>
                      <div className="mono">Produto</div>
                      <div>{o.productName ?? '—'}</div>
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
