## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## 2024-06-21 - Add ARIA Attributes to Inline Error Messages
**Learning:** Found multiple instances where inline error messages (specifically those styled with `errorInline`) were missing `role="alert"` and `aria-live="assertive"`. This causes screen readers to ignore dynamic error messages when a user attempts to submit a form or faces a validation error.
**Action:** Always ensure that dynamically rendered error messages, especially inline ones like validation errors, include `role="alert"` and `aria-live="assertive"` so they are properly announced to screen reader users when they appear.
