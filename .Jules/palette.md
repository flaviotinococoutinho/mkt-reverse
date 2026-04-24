## 2024-05-18 - [Pager Component a11y Fix]
**Learning:** For dynamic pagination readouts, using aria-live without aria-atomic means only changed numbers get read. The test suite strictly validates localized Portuguese strings for all ARIA labels.
**Action:** When working on pagination or counters, always apply aria-atomic="true" alongside aria-live="polite". Always preserve and strictly use localized Portuguese strings in UI and tests.
