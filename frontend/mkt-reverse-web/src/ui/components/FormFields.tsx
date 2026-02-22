import React from 'react'

export function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="field">
      <div className="label">{label}</div>
      {children}
    </label>
  )
}

export function TextInput(props: React.ComponentPropsWithoutRef<'input'>) {
  return <input {...props} className={['input', props.className].filter(Boolean).join(' ')} />
}

export function Select(props: React.ComponentPropsWithoutRef<'select'>) {
  return <select {...props} className={['select', props.className].filter(Boolean).join(' ')} />
}

