## 2024-03-11 - Add ARIA Labels to Pagination Pager Controls
**Learning:** Found a specific component (`Pager.tsx`) utilizing icon-only buttons ("←" and "→") for previous/next navigation without accompanying ARIA labels, making it inaccessible to screen readers. Also lacked a descriptive `role="navigation"` to establish it as pagination.
**Action:** When working on generic shared controls (like paginators, carousels), ensure that icon-only interactive elements carry descriptive `aria-label`s. Wrapper elements for distinct navigation zones must use `nav` or `role="navigation"` coupled with a meaningful `aria-label` like "Pagination".

## 2024-03-12 - Interpolate identifying values in dynamic list item ARIA labels
**Learning:** Found an issue in `AttributeEditor.tsx` where an icon-only "remove" button in a dynamic list of attributes had a generic `title` ("Remover atributo") but no `aria-label`. Without specific context, screen readers would announce "Remover atributo" for every row, making it ambiguous which item is being removed.
**Action:** When implementing actions (like delete/remove) for items in a dynamic list, always interpolate the item's identifying value into the `aria-label` (e.g., `aria-label={attr.key ? \`Remover atributo ${attr.key}\` : \`Remover atributo ${index + 1}\`}`) to provide clear context for screen reader users.
