## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-15 - Interpolating identifiers into ARIA labels for dynamic lists
**Learning:** For dynamic lists of items (like a key-value attribute editor), a generic "Remove" `aria-label` on each item's delete button provides insufficient context for screen reader users navigating the list sequentially.
**Action:** Always interpolate the item's identifying value (e.g., its key or an index) into the `aria-label` for list item actions (e.g., `aria-label="Remove attribute Size"`) to provide specific context.
