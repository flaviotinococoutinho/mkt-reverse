## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-18 - Interpolated ARIA Labels for Dynamic List Actions
**Learning:** For dynamic lists of key-value attribute editors, static `aria-label`s on remove buttons (e.g., "Remover atributo") cause confusion for screen reader users as multiple identical buttons are read aloud without identifying which item will be removed.
**Action:** Always interpolate the item's identifying value (like the attribute name/key) into the `aria-label` for list item actions (e.g., `Remover atributo ${attr.key}`) to provide specific context for screen readers.
