## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## 2024-05-24 - Accessibility for Inline Error Messages
**Learning:** Found a pattern where inline validation error messages (often styled with `.errorInline`) lacked ARIA attributes. Without `role="alert"` and `aria-live="assertive"`, asynchronous error states (such as form submission failures) are not announced to screen readers, making forms inaccessible when they fail.
**Action:** When adding or updating inline error messages that appear asynchronously, ensure they include `role="alert"` and `aria-live="assertive"` to immediately announce the failure to screen readers.
