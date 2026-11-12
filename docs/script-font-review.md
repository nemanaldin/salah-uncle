# Script and font check (2026-11-12)

The existing Vazirmatn regular cmap was inspected locally using `fc-query`; no fonts were downloaded. It covers the checked Urdu letters ٹ ڈ ڑ ں ھ ہ ے ۓ and Pashto letters ټ ځ څ ډ ړ ږ ښ ګ ڼ ۍ ې. Arabic and these extended letters keep the existing font family. This is a cmap check, not a visual shaping/TalkBack certification.

Vazirmatn does not contain Bengali glyphs. Bengali now explicitly uses Android's system fallback family for every typography role; language-specific locale hints, font padding and untrimmed line-height metrics avoid using Persian font metrics for clipped Bengali clusters. Existing fonts and all translations are unchanged. Rendering, mixed-script diacritics, 200% font scale and OEM fallback availability still need a real Android target; none is attached locally and /dev/kvm is unavailable.

Visibility recovery changes only the persisted school and language fields, preserving the user's Asr/method configuration and every bookmark. A hidden saved language takes the first active system locale, then English. Changes to the active-list fingerprint trigger recovery again, while existing active language choices continue to win on ordinary launches.
