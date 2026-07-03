## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## $(date +%Y-%m-%d) - Adding ARIA alert roles to inline error messages
**Learning:** Inline error messages (styled with `.errorInline`) in React components may lack critical accessibility attributes by default. A recurring pattern here is that dynamic asynchronous feedback (e.g., failed mutations) is rendered without alerting screen readers.
**Action:** When adding or auditing inline error components, always ensure they include `role="alert"` and `aria-live="assertive"` so visually impaired users immediately hear validation and submission failures.

## 2025-02-12 - Adding ARIA alert roles to inline error messages
**Learning:** Inline error messages (styled with `.errorInline`) in React components may lack critical accessibility attributes by default. A recurring pattern here is that dynamic asynchronous feedback (e.g., failed mutations) is rendered without alerting screen readers.
**Action:** When adding or auditing inline error components, always ensure they include `role="alert"` and `aria-live="assertive"` so visually impaired users immediately hear validation and submission failures.
