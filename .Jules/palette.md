## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## 2024-05-24 - Dynamic ARIA Labels and Live Regions in Forms and Pagination
**Learning:** React error messages without `role="alert"` and `aria-live="assertive"` aren't announced to screen readers. Pagination counters that dynamically update (like "page 1 / 5") need `aria-atomic="true"` on their `aria-live` region, otherwise screen readers might only announce the number that changed instead of the full context.
**Action:** Always add `role="alert"` and `aria-live="assertive"` to inline form or submission errors. Always add `aria-atomic="true"` to dynamic text components inside an `aria-live` container.
