import React, { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'

import { createEvent } from '@/api/sourcing'
import { AttributeEditor } from '@/ui/components/AttributeEditor'
import { SpecAttribute } from '@/ui/components/AttributeEditor'
import { useAuth } from '@/ui/context/AuthContext'

const defaultAttributes: SpecAttribute[] = [
  { key: 'voltage', type: 'VOLTAGE', unit: 'V', value: 220 },
  { key: 'color', type: 'COLOR', value: 'preto' },
]

export function CreateEventPage() {
  const nav = useNavigate()
  const { user } = useAuth() // Get current user from Auth Context
  const [attributes, setAttributes] = useState<SpecAttribute[]>(defaultAttributes)
  const [form, setForm] = useState({
    // Form fields will be initialized from user context if available
    title: '',
    description: '',
    mccCategoryCode: 5533, // Default for MVP
    productName: '',
    productDescription: '',
    category: 'part',
    unitOfMeasure: 'UN',
    quantityRequired: 1,
    validForHours: 24,
    estimatedBudgetCents: undefined,
  })

  // Initialize form with user data when user changes
  React.useEffect(() => {
    if (user) {
      setForm(prev => ({
        ...prev,
        tenantId: user.tenantId || prev.tenantId,
        buyerOrganizationId: user.buyerOrganizationId || prev.buyerOrganizationId,
        buyerContactName: user.buyerContactName || prev.buyerContactName,
        buyerContactPhone: user.buyerContactPhone || prev.buyerContactPhone,
      }))
    }
  }, [user])

  const mutation = useMutation({
    mutationFn: async () => {
      return createEvent({
        tenantId: form.tenantId,
        buyerOrganizationId: form.buyerOrganizationId,
        buyerContactName: form.buyerContactName,
        buyerContactPhone: form.buyerContactPhone,
        title: form.title,
        description: form.description,
        mccCategoryCode: Number(form.mccCategoryCode),
        productName: form.productName,
        productDescription: form.productDescription,
        category: form.category,
        unitOfMeasure: form.unitOfMeasure,
        quantityRequired: Number(form.quantityRequired),
        validForHours: Number(form.validForHours),
        estimatedBudgetCents: form.estimatedBudgetCents ? Number(form.estimatedBudgetCents) : undefined,
        attributes: attributes, // Send the edited attributes array
      })
    },
    onSuccess: (event) => {
      nav(`/events/${event.id}`)
    },
  })

  const handleFieldChange = (field: string, value: string | number) => {
    setForm(prev => ({ ...prev, [field]: value }))
  }

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    // Basic validation
    if (!form.tenantId || !form.buyerOrganizationId || !form.productName) {
      alert("Preencha os campos obrigatórios (Tenant, Org, Produto)")
      return
    }

    mutation.mutate()
  }

  return (
    <section className="page">
      <div className="pageHeader">
        <h1 className="h1">Criar pedido (sourcing-event)</h1>
        <p className="lede">
          Editor de atributos visual para facilitar a validação.
          Campos preenchidos automaticamente se estiver logado.
        </p>
      </div>

      {!user && (
        <div style={{ padding: '1rem', background: '#fff3cd', color: '#721c24', borderRadius: '8px', marginBottom: '1rem' }}>
          <strong>Atenção:</strong> Usuário não autenticado. O formulário está em modo <em>Demo</em> (dados fixos mockados) para permitir testes.
        </div>
      )}

      <div className="panel">
        <form className="form" onSubmit={handleSubmit}>
          <div className="twoCol">
            <label className="field">
              <div className="label">tenantId</div>
              <input
                className="input"
                value={form.tenantId}
                onChange={(e) => handleFieldChange('tenantId', e.target.value)}
                disabled={!!user} // Disable if logged in (auto-filled)
                placeholder="ex: tenant-1"
              />
            </label>
            <label className="field">
              <div className="label">buyerOrganizationId</div>
              <input
                className="input"
                value={form.buyerOrganizationId}
                onChange={(e) => handleFieldChange('buyerOrganizationId', e.target.value)}
                disabled={!!user}
                placeholder="ex: org-1"
              />
            </label>
          </div>

          <div className="twoCol">
            <label className="field">
              <div className="label">buyerContactName</div>
              <input
                className="input"
                value={form.buyerContactName}
                onChange={(e) => handleFieldChange('buyerContactName', e.target.value)}
                disabled={!!user}
              />
            </label>
            <label className="field">
              <div className="label">buyerContactPhone</div>
              <input
                className="input"
                value={form.buyerContactPhone}
                onChange={(e) => handleFieldChange('buyerContactPhone', e.target.value)}
                disabled={!!user}
              />
            </label>
          </div>

          <label className="field">
            <div className="label">title</div>
            <input
              className="input"
              value={form.title}
              onChange={(e) => handleFieldChange('title', e.target.value)}
            />
          </label>

          <label className="field">
            <div className="label">description</div>
            <textarea
              className="textarea"
              rows={3}
              value={form.description}
              onChange={(e) => handleFieldChange('description', e.target.value)}
            />
          </label>

          <div className="twoCol">
            <label className="field">
              <div className="label">mccCategoryCode</div>
              <input
                className="input"
                inputMode="numeric"
                value={form.mccCategoryCode}
                onChange={(e) => handleFieldChange('mccCategoryCode', e.target.value)}
              />
            </label>
            <label className="field">
              <div className="label">validForHours</div>
              <input
                className="input"
                inputMode="numeric"
                value={form.validForHours}
                onChange={(e) => handleFieldChange('validForHours', e.target.value)}
              />
            </label>
          </div>

          <div className="twoCol">
            <label className="field">
              <div className="label">productName</div>
              <input
                className="input"
                value={form.productName}
                onChange={(e) => handleFieldChange('productName', e.target.value)}
              />
            </label>
            <label className="field">
              <div className="label">quantityRequired</div>
              <input
                className="input"
                inputMode="numeric"
                value={form.quantityRequired}
                onChange={(e) => handleFieldChange('quantityRequired', e.target.value)}
              />
            </label>
          </div>

          <div className="twoCol">
            <label className="field">
              <div className="label">unitOfMeasure</div>
              <input
                className="input"
                value={form.unitOfMeasure}
                onChange={(e) => handleFieldChange('unitOfMeasure', e.target.value)}
              />
            </label>
            <label className="field">
              <div className="label">estimatedBudgetCents</div>
              <input
                className="input"
                inputMode="numeric"
                value={form.estimatedBudgetCents}
                onChange={(e) => handleFieldChange('estimatedBudgetCents', e.target.value)}
                placeholder="Opcional (centavos)"
              />
            </label>
          </div>

          <div className="field">
            <div className="label">Atributos do Produto</div>
            {/* Replaced manual JSON textarea with AttributeEditor component */}
            <div style={{ marginTop: '0.5rem', marginBottom: '1rem', border: '1px solid #eee', borderRadius: '8px', background: '#fafafa', padding: '1rem' }}>
              <div style={{ marginBottom: '0.5rem' }}>
                <small>Selecione o tipo (ex: VOLTAGE para eletrônica, COLOR para cor) e informe o valor.</small>
              </div>
              <AttributeEditor
                attributes={attributes}
                onChange={setAttributes}
                error={mutation.isError ? (mutation.error as any)?.message : null}
              />
            </div>
          </div>

          <div className="actions">
            <button className="btn" type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? 'Criando…' : 'Criar pedido'}
            </button>
            {mutation.isError && (
              <div role="alert" className="errorInline">
                Erro ao criar pedido. Tente novamente.
              </div>
            )}
          </div>
        </form>
      </div>
    </section>
  )
}
