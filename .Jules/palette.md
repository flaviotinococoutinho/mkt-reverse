## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## 2024-06-25 - Atomic Updates for ARIA Live Regions
**Learning:** When using `aria-live` regions to announce dynamic text changes (e.g., a pagination counter like "page 1 / 5"), screen readers may only announce the isolated part that changed (e.g., just "2" instead of "page 2 / 5") if the region is not atomic.
**Action:** Add `aria-atomic="true"` to `aria-live` containers that display stateful text like pagination counters to ensure the entire content of the region is announced as a single, coherent update.
