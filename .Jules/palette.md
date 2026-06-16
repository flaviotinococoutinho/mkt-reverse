## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## 2026-06-16 - Inline Error Messages Without ARIA Alerts
**Learning:** Discovered that inline error messages (specifically those using the `errorInline` class) across multiple components (`AttributeEditor`, `CreateEventPage`, `EventDetailPage`) lacked `role="alert"` and `aria-live="assertive"`, preventing screen readers from automatically announcing validation failures or submission errors when they appear.
**Action:** Always ensure that dynamically appearing error messages or validation feedback text blocks include `role="alert"` and `aria-live="assertive"` so that screen reader users are immediately informed of the issue without having to navigate to the error text.
