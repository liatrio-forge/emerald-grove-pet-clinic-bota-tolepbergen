# 02 Questions Round 1 - Language Selector

Please answer each question below (select one or more options, or add your own notes). Feel free to add additional context under any question.

## 1. Which languages should be included in the selector?

The codebase already has translations for 8 languages (EN, ES, DE, PT, KO, RU, FA, TR). The issue says "keep initial language list small (e.g., EN/ES/DE)."

- [x] (A) Only EN, ES, DE (as the issue suggests — 3 languages)
- [ ] (B) All 8 existing languages (EN, ES, DE, PT, KO, RU, FA, TR) since translations already exist
- [ ] (C) A custom subset (specify which ones below)
- [ ] (D) Other (describe)

> Rationale: The issue explicitly says "Keep initial language list small (e.g., EN/ES/DE)." We follow the issue as written. Additional languages can be added later easily.

## 2. UI Component Style

How should the language selector look in the header navbar?

- [x] (A) Bootstrap dropdown button showing the current language name/flag, with other languages in the dropdown menu
- [ ] (B) Simple inline links (e.g., "EN | ES | DE") displayed directly in the navbar
- [ ] (C) A `<select>` dropdown (standard HTML select element) in the navbar
- [ ] (D) Other (describe)

> Rationale: A Bootstrap dropdown is consistent with the existing navbar design (Bootstrap 5.3.8), scales well if more languages are added, and looks professional.

## 3. Language Display Format

How should each language be represented in the selector?

- [ ] (A) Language code only (e.g., "EN", "ES", "DE")
- [ ] (B) Full language name in English (e.g., "English", "Spanish", "German")
- [x] (C) Full language name in its own language (e.g., "English", "Español", "Deutsch")
- [ ] (D) Flag emoji + language name (e.g., "🇺🇸 English", "🇪🇸 Español")
- [ ] (E) Other (describe)

> Rationale: Showing each language in its own name is the most user-friendly and internationally standard approach. Users can find their language even if they don't read English. Avoids flag emojis which can be politically sensitive and render inconsistently.

## 4. Selector Placement

Where in the navbar should the language selector appear?

- [x] (A) Far right of the navbar, after all navigation items (most common pattern)
- [ ] (B) Far left, before the navigation items but after the brand/logo
- [ ] (C) Inside the existing navigation items list, as the last item
- [ ] (D) Other (describe)

> Rationale: Far-right placement is the most common web convention for language selectors. It's visually separated from navigation and easy to find.

## 5. E2E Test Scope

The issue asks for a Playwright E2E test. What should it verify?

- [ ] (A) Minimal: switch language once and assert one translated heading changes
- [x] (B) Moderate: switch language, assert nav labels + page heading change, navigate to another page and assert language persists
- [ ] (C) Thorough: test all supported languages, verify multiple elements per language, verify persistence across navigation
- [ ] (D) Other (describe)

> Rationale: Moderate scope covers all three acceptance criteria (visible selector, text updates, persistence across navigation) without over-testing. Matches the issue's proof requirements.
