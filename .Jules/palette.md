## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## 2026-05-06 - Add aria-atomic to dynamic text in aria-live regions
**Learning:** When using `aria-live` on regions with dynamic text (like pagination counters 'page 1 / 5'), if only a part of the text changes (e.g. the number 1 to 2), screen readers might only announce the isolated change rather than the full context. Also, `aria-current="page"` is meant for interactive active links, not plain text containers.
**Action:** Use `aria-atomic="true"` on `aria-live` dynamic text regions to ensure screen readers read the full state change. Avoid using `aria-current="page"` on non-navigational plain text elements.
