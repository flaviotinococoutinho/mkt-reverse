import React, { useState } from 'react'
import { XMarkIcon } from '@heroicons/react/24/solid'

// Mapping Java SpecAttributeType to local UI options for cleaner code
// Based on backend: SpecAttributeType { TEXT, NUMBER, BOOLEAN, ENUM, WEIGHT, VOLUME, VOLTAGE, LANGUAGE, COLOR }
const ATTRIBUTE_TYPES = [
  { value: 'TEXT', label: 'Texto' },
  { value: 'NUMBER', label: 'Número' },
  { value: 'BOOLEAN', label: 'Sim/Não (Boolean)' },
  { value: 'ENUM', label: 'Enumeração' },
  { value: 'VOLTAGE', label: 'Voltagem' },
  { value: 'WEIGHT', label: 'Peso' },
  { value: 'VOLUME', label: 'Volume' },
  { value: 'COLOR', label: 'Cor' },
] as const

export type SpecAttribute = {
  key: string
  type: string
  value: unknown
  unit?: string
}

export interface AttributeEditorProps {
  attributes: SpecAttribute[]
  onChange: (attrs: SpecAttribute[]) => void
  error?: string | null
}

export function AttributeEditor({ attributes, onChange, error }: AttributeEditorProps) {
  const [localAttributes, setLocalAttributes] = useState<SpecAttribute[]>(attributes || [])

  const updateAttribute = (index: number, field: keyof SpecAttribute, value: SpecAttribute[keyof SpecAttribute]) => {
    const updated = [...localAttributes]
    updated[index] = { ...updated[index], [field]: value }
    setLocalAttributes(updated)
    onChange(updated)
  }

  const addAttribute = () => {
    const newAttr: SpecAttribute = { key: '', type: 'TEXT', value: '' }
    setLocalAttributes([...localAttributes, newAttr])
    onChange([...localAttributes, newAttr])
  }

  const removeAttribute = (index: number) => {
    const updated = localAttributes.filter((_, i) => i !== index)
    setLocalAttributes(updated)
    onChange(updated)
  }

  return (
    <div className="panel">
      <h3 className="h3" style={{ marginBottom: '1rem' }}>
        Atributos do Produto
      </h3>

      {error && (
        <div role="alert" className="errorInline" style={{ marginBottom: '1rem', padding: '0.75rem', background: '#fee2e2', borderRadius: '4px' }}>
          {error}
        </div>
      )}

      <div className="form">
        {localAttributes.map((attr, index) => (
          <div key={index} style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', alignItems: 'flex-start', padding: '1rem', border: '1px solid #eee', borderRadius: '8px', background: '#fafafa' }}>
            <div style={{ flex: 1, display: 'grid', gap: '0.5rem' }}>
              {/* Key */}
              <label className="field" style={{ display: 'block' }}>
                <div className="label">Nome (Key)</div>
                <input
                  className="input"
                  type="text"
                  value={attr.key}
                  onChange={(e) => updateAttribute(index, 'key', e.target.value)}
                  placeholder="ex: voltage, cor"
                />
              </label>

              {/* Type Selector */}
              <label className="field" style={{ display: 'block' }}>
                <div className="label">Tipo</div>
                <select
                  className="input"
                  value={attr.type}
                  onChange={(e) => updateAttribute(index, 'type', e.target.value)}
                >
                  {ATTRIBUTE_TYPES.map(t => (
                    <option key={t.value} value={t.value}>{t.label}</option>
                  ))}
                </select>
              </label>
            </div>

            <div style={{ flex: 1, display: 'grid', gap: '0.5rem' }}>
              {/* Value Input - Dynamic based on Type */}
              <label className="field" style={{ display: 'block' }}>
                <div className="label">Valor</div>
                {attr.type === 'BOOLEAN' ? (
                  <select
                    className="input"
                    value={String(attr.value)}
                    onChange={(e) => updateAttribute(index, 'value', e.target.value === 'true')}
                  >
                    <option value="true">Sim</option>
                    <option value="false">Não</option>
                  </select>
                ) : (
                  <input
                    className="input"
                    type={attr.type === 'NUMBER' || attr.type === 'VOLTAGE' || attr.type === 'WEIGHT' || attr.type === 'VOLUME' ? 'number' : 'text'}
                    value={String(attr.value)}
                    onChange={(e) => updateAttribute(index, 'value', e.target.value)}
                    placeholder="Valor do atributo"
                  />
                )}
              </label>

              {/* Unit (Optional) */}
              {(attr.type === 'WEIGHT' || attr.type === 'VOLUME' || attr.type === 'VOLTAGE') && (
                <label className="field" style={{ display: 'block' }}>
                  <div className="label">Unidade</div>
                  <input
                    className="input"
                    type="text"
                    value={attr.unit || ''}
                    onChange={(e) => updateAttribute(index, 'unit', e.target.value)}
                    placeholder="ex: V, kg, mL"
                  />
                </label>
              )}

              {/* Remove Button */}
              <button
                type="button"
                onClick={() => removeAttribute(index)}
                className="btn btn-secondary"
                style={{ padding: '0.25rem 0.5rem' }}
                title="Remover atributo"
                aria-label={attr.key ? `Remover atributo ${attr.key}` : "Remover atributo"}
              >
                <XMarkIcon style={{ width: '16px', height: '16px' }} />
              </button>
            </div>
          </div>
        ))}

        <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
          <button type="button" onClick={addAttribute} className="btn btn-secondary">
            + Adicionar Atributo
          </button>
        </div>
      </div>
    </div>
  )
}
