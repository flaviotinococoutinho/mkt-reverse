## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## 2024-06-04 - Dynamic Pagination Text and aria-atomic
**Learning:** Found a specific issue in `Pager.tsx` where an `aria-live` region ("page X / Y") was announcing confusingly to screen readers because it lacked `aria-atomic`. Additionally, the test for this component incorrectly asserted `aria-current="page"` on a non-interactive informational `div`, which is semantically incorrect (it should only be applied to interactive active links or buttons).
**Action:** When creating or maintaining dynamic text elements (e.g., counters or pagination displays) that act as `aria-live` regions, ensure `aria-atomic="true"` is set so the screen reader reads the complete context, rather than just the isolated piece of changing text. Avoid placing `aria-current` on static text wrappers.
