## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-04-01 - Add Dynamic ARIA Labels to List Items and Role Alert to Errors
**Learning:** Found dynamic list items in `AttributeEditor.tsx` where identical icon-only "Remove" buttons had no context of what they were removing, which creates confusion for screen reader users traversing lists. Additionally, inline error messages lacked an alert role for immediate announcement.
**Action:** Always interpolate specific context (like the item's name or key) into the `aria-label` for repeated actions in dynamic lists (e.g., "Remove attribute Size"). Always wrap dynamic error messages with `role="alert"` so they are read aloud immediately when they appear.
