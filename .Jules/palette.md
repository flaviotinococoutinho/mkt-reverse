## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## 2024-05-30 - ARIA Attributes for Pagination Elements
**Learning:** Found an accessibility anti-pattern in the pagination component where an informational text div (e.g. "page 1 / 5") was marked with `aria-current="page"`. This attribute should only be used on interactive semantic navigation items (like links or buttons) that represent the currently active page. In addition, when updating `aria-live` regions containing dynamic text strings (like paginators), it is crucial to include `aria-atomic="true"` to ensure screen readers announce the entire string, rather than just the partial update.
**Action:** Remove `aria-current="page"` from generic status text elements. When adding `aria-live` properties to dynamically changing content that should be announced as a cohesive unit, always use `aria-atomic="true"`.
