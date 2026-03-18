## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".
## 2024-05-19 - Context-Aware ARIA Labels for Dynamic Lists
**Learning:** When users edit dynamic key-value lists (like the AttributeEditor), generic "Remove" ARIA labels on icon buttons create a confusing experience for screen reader users, as they cannot distinguish which item will be removed.
**Action:** Always interpolate the list item's identifying value (e.g., `attr.key`) into the `aria-label` (e.g., "Remover atributo voltage"). If the item is new/unnamed, provide a fallback using its index.
