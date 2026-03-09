import React from 'react'

type Settings = {
  tenantId: string
  supplierId: string
}

const KEY = 'mkt-reverse/settings'

function loadSettings(): Settings {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return { tenantId: 'tenant-1', supplierId: 'supplier-1' }
    const parsed = JSON.parse(raw) as Partial<Settings>
    return {
      tenantId: parsed.tenantId ?? 'tenant-1',
      supplierId: parsed.supplierId ?? 'supplier-1',
    }
  } catch {
    return { tenantId: 'tenant-1', supplierId: 'supplier-1' }
  }
}

export function SettingsDock() {
  const [open, setOpen] = React.useState(false)
  const [settings, setSettings] = React.useState<Settings>(() => loadSettings())

  React.useEffect(() => {
    localStorage.setItem(KEY, JSON.stringify(settings))
  }, [settings])

  return (
    <div className="dock">
      <button
        className="btn ghost"
        onClick={() => setOpen((v) => !v)}
        aria-expanded={open}
        aria-controls="settings-dock-panel"
        aria-label="Toggle settings dock"
      >
        <span className="mono">ctx</span>
      </button>

      {open && (
        <div
          id="settings-dock-panel"
          className="dockPanel"
          role="dialog"
          aria-label="Context settings"
        >
          <div className="dockTitle">Context</div>
          <div className="dockHint">
            IDs here are not “app state”; they only parameterize API calls.
          </div>

          <label className="field">
            <div className="label">tenantId</div>
            <input
              className="input"
              value={settings.tenantId}
              onChange={(e) =>
                setSettings((s) => ({ ...s, tenantId: e.target.value }))
              }
            />
          </label>

          <label className="field">
            <div className="label">supplierId</div>
            <input
              className="input"
              value={settings.supplierId}
              onChange={(e) =>
                setSettings((s) => ({ ...s, supplierId: e.target.value }))
              }
            />
          </label>

          <div className="dockActions">
            <button className="btn" onClick={() => setOpen(false)}>
              Fechar
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

export function useAppSettings(): Settings {
  const [settings, setSettings] = React.useState<Settings>(() => loadSettings())

  React.useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (e.key === KEY) setSettings(loadSettings())
    }
    window.addEventListener('storage', onStorage)
    return () => window.removeEventListener('storage', onStorage)
  }, [])

  return settings
}

