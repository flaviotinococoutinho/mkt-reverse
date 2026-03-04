## 2024-05-24 - Missing ARIA Labels on Icon-Only Buttons
**Learning:** Found several icon-only buttons across the frontend (`Pager.tsx`, `AttributeEditor.tsx`, `SettingsDock.tsx`) that used generic `onClick` handlers but completely lacked accessible names, making them invisible or confusing to screen readers.
**Action:** When adding or reviewing custom icon components (like Heroicons) used as buttons, always verify that an `aria-label` or visually hidden text is present so assistive technologies can describe the button's purpose.
