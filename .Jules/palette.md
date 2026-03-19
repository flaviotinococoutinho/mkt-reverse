## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".
## 2026-03-19 - Dynamic ARIA labels for dynamic lists
**Learning:** In dynamic list editors, icon-only action buttons (like "Remove") often lack specific context, making them ambiguous for screen reader users when multiple list items exist.
**Action:** Always interpolate the list item's identifying value (e.g., its `key` or name) into the `aria-label` for the action button (e.g., `aria-label={"Remover atributo ${attr.key || index + 1}"}`) to provide specific context. Also, ensure inline validation errors use `role="alert"` to be announced immediately.
