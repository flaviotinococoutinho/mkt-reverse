## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.
## 2024-10-27 - Added ARIA Roles to Inline Error Messages
**Learning:** Found several inline error messages (`<div className="errorInline">`) displayed upon form submission errors (e.g., in `CreateEventPage.tsx` and `EventDetailPage.tsx`) that lacked proper ARIA attributes, meaning screen readers would not dynamically announce the error.
**Action:** Always add `role="alert"` and `aria-live="assertive"` to inline error message containers that appear dynamically after a user action, so screen reader users are immediately informed of the failure.
