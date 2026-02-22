# Visual identity — mkt-reverse (MVP)

Direction: **“O Leilão”** — industrial/editorial, with warm paper surfaces on a dark ink base.

The goal is to feel like a procurement “floor”: practical, terse, high signal.

## Palette

### Core
- **Ink / background**: `#0A0E14`
- **Paper surface (overlay)**: `rgba(255, 247, 237, 0.06)`
- **Stroke**: `rgba(233, 230, 223, 0.16)`

### Accents
- **Citrus (primary)**: `#FFB000`
- **Mint glitch (secondary)**: `#62FFB8`
- **Danger**: `#FF4D6D`

## Typography

- Display: **Instrument Serif** (used sparingly for titles)
- Body: **Instrument Sans**
- Mono: **Spline Sans Mono** (IDs, meta, “procurement vibes”)

## UI principles (to keep state in the backend)

- Treat UI as **server-state**: lists, details, responses are all read from API.
- Mutations are **POSTs** and UI refresh is **invalidate → refetch**.
- The only local storage is **context parameters** (tenantId/supplierId) to avoid hardcoding.

