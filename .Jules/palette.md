## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## 2024-06-14 - Semantic Usage of aria-current and aria-atomic in Pagination
**Learning:** Found a component test that incorrectly asserted `aria-current="page"` on a non-interactive, purely informational text container (`<div class="pagerText">page 1 / 5</div>`). Additionally, the `aria-live` region updating the page numbers lacked `aria-atomic="true"`, causing screen readers to potentially announce only the changed number instead of the full context ("page 2 / 5").
**Action:** When implementing pagination, apply `aria-current="page"` only to interactive semantic navigation items (like the active page link or button). For dynamic text counters inside `aria-live` regions, always add `aria-atomic="true"` to ensure the whole phrase is announced coherently when the count changes.
