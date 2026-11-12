# Reading modes and exact source navigation

Only the selected school's `prayer-*`, `purification-*` guides and the existing `ghusl` article offer slides. Other articles stay continuous. The text-mode switch retains every source section and source reference. Neither mode writes completion progress or introduces a course gate.

Slides use Compose Foundation `HorizontalPager` with its normal `reverseLayout=false` behavior and the theme's `LocalLayoutDirection`: next is a rightward finger swipe in RTL and a leftward swipe in LTR. Previous/next buttons and the localized step counter are always available. This is a code review of standard pager behavior, not a claim of device gesture verification.

`sentenceSlices` splits only after sentence-ending punctuation followed by whitespace; concatenating the slices restores the source exactly, including whitespace. Long sentences are not summarized or cut just to fit the screen. A slide's body can scroll internally for long sentences and 200% text; vertical movement does not navigate between steps. This is the chosen accessibility assumption where fixed-height short text and unchanged source wording would conflict.

Search results carry the exact source character offset. Section bookmarks retain source IDs and an optional offset, so a hit/bookmark within a split section opens the matching slide. Contents entries open the first slide for their source section. Existing topic stars and original content/media files are unchanged. New phase-13 pictures are described in stage-artwork-review.md; all remain draft artwork pending owner review.
