## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-05-24 - Dynamic ARIA Labels in Attribute Lists
**Learning:** When dealing with dynamic lists of inputs (like key-value attribute editors), icon-only remove buttons need specific, dynamic `aria-label`s (e.g., "Remove attribute Size") rather than generic ones ("Remove attribute") so screen reader users know exactly which item they are deleting.
**Action:** Always interpolate the item's identifying value into the `aria-label` for list item actions.

## 2024-10-25 - ARIA Current attributes and Localization in Tests
**Learning:** Adding `aria-current="page"` to the current page indicator in a pagination component is an excellent micro-UX addition for screen readers. However, when writing tests in an app with strict localization (e.g. Portuguese), standard English ARIA conventions (like expecting "Pagination" or "Previous page") will cause tests to fail. The tests must assert the exact localized strings.
**Action:** Ensure that any ARIA attributes added to generic components correctly respect the localization language of the application (e.g., Portuguese "Paginação" instead of "Pagination") and that unit tests match these strings exactly.