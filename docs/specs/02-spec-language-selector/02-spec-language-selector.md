# 02-spec-language-selector

## Introduction/Overview

The Emerald Grove Veterinary Clinic application already supports multiple languages via URL parameter (`?lang=xx`) and session-based locale persistence, but there is no visible UI element for users to switch languages. This feature adds a language selector dropdown to the global header navbar so users can switch between English, Spanish, and German without manually editing the URL.

## Goals

- Provide a visible, accessible language selector in the header on every page
- Allow users to switch between English, Spanish, and German with a single click
- Persist the selected language across page navigation within the same session
- Follow existing Bootstrap 5 navbar patterns and Thymeleaf fragment conventions
- Maintain full i18n sync test compliance (no broken translations)

## User Stories

- **As a Spanish-speaking user**, I want to click a language selector in the header so that I can view the entire application in my language without editing the URL.
- **As a user browsing in German**, I want my language choice to persist as I navigate between pages so that I don't have to reselect my language on every page.
- **As any user**, I want to see which language is currently active so that I know what language the site is displayed in.

## Demoable Units of Work

### Unit 1: Language Selector Dropdown in Header

**Purpose:** Add a visible Bootstrap dropdown to the navbar that lets users switch languages. This is the core UI component that addresses the main gap.

**Functional Requirements:**
- The system shall display a Bootstrap dropdown in the navbar, positioned to the right of the existing navigation items
- The dropdown trigger shall display the name of the currently active language in its native form (e.g., "English", "Español", "Deutsch")
- The dropdown menu shall list all three supported languages (English, Español, Deutsch)
- When a user selects a language from the dropdown, the system shall navigate to the current page with the `?lang=xx` parameter appended
- The dropdown shall be visible and functional on all pages that use the shared layout template
- The dropdown shall work correctly on mobile (collapsed navbar) and desktop viewports
- The currently active language shall be visually distinguished in the dropdown menu (e.g., bold or checkmark)

**Proof Artifacts:**
- Screenshot: Homepage shown in English with the dropdown visible in the header
- Screenshot: Same page shown in Spanish after selecting "Español" from the dropdown
- Screenshot: Same page shown in German after selecting "Deutsch" from the dropdown
- Unit Test: Controller/view test verifying the dropdown renders with correct language options

### Unit 2: Session Persistence and E2E Verification

**Purpose:** Verify that language selection persists across navigation and provide automated E2E test coverage as specified in the issue's acceptance criteria.

**Functional Requirements:**
- The system shall persist the selected language across page navigation within the same session (using the existing `SessionLocaleResolver`)
- After selecting a language on one page, navigating to a different page shall display that page in the selected language without requiring reselection
- The dropdown shall reflect the currently active language on every page after navigation

**Proof Artifacts:**
- Playwright E2E test: Switches language to Spanish, asserts nav labels and page heading change, navigates to another page, and asserts the language persists
- Playwright E2E test: Switches language to German and verifies the same persistence behavior

## Non-Goals (Out of Scope)

1. **Adding new languages**: Only EN, ES, DE will be in the selector. Other existing translations (PT, KO, RU, FA, TR) are not included in the dropdown for this iteration
2. **Language auto-detection**: The system will not detect browser language preferences or geo-location to set a default
3. **Persistent language preference beyond session**: Language choice will not be saved to a cookie or database — it resets when the session ends (existing behavior)
4. **Translation of language names**: The language names in the dropdown are always in their native form regardless of the currently selected UI language

## Design Considerations

- The dropdown should use Bootstrap 5 `dropdown` component classes to match the existing navbar styling
- On the dark navbar, the dropdown trigger should use `nav-link` styling with a `fa-globe` Font Awesome icon to visually indicate language switching
- On mobile viewports, the dropdown should appear inside the collapsed navbar menu
- The currently active language should be visually distinguished (Bootstrap `active` class or similar)

## Repository Standards

- **TDD**: All production code must follow Red-Green-Refactor cycle (tests written first)
- **Testing**: JUnit 5 + MockMvc for controller tests, Playwright for E2E tests
- **I18n sync**: New message keys must be added to all language files to pass `I18nPropertiesSyncTest`
- **Templates**: Use Thymeleaf fragment patterns consistent with `layout.html`
- **Commits**: Use conventional commit format

## Technical Considerations

- The `layout.html` fragment is shared across all pages — the dropdown only needs to be added once
- The existing `LocaleChangeInterceptor` handles `?lang=xx` parameters — the dropdown links just need to append this parameter
- Thymeleaf's `#locale.language` object provides the current locale for highlighting the active language
- New message keys (e.g., `lang.en`, `lang.es`, `lang.de`) must be added to `messages.properties`, `messages_es.properties`, and `messages_de.properties` — and also to all other language files (PT, KO, RU, FA, TR) to satisfy `I18nPropertiesSyncTest`
- No new Java classes or Spring beans are needed — this is purely a template change with new message keys

## Security Considerations

No specific security considerations identified. The `?lang=xx` parameter is already sanitized by Spring's `LocaleChangeInterceptor`, and no user data is involved.

## Success Metrics

1. **Visibility**: Language selector dropdown is visible in the header on all pages
2. **Functionality**: Clicking a language option updates all visible UI text to the selected language
3. **Persistence**: Selected language persists when navigating to different pages in the same session
4. **Test coverage**: Unit tests and Playwright E2E tests pass, covering all three acceptance criteria
5. **I18n compliance**: `I18nPropertiesSyncTest` passes with the new message keys

## Open Questions

No open questions at this time.
