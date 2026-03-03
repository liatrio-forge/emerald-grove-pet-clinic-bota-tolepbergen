# 02 Tasks - Language Selector

## Relevant Files

- `src/main/resources/messages/messages.properties` - Base message file; add new `lang.*` keys
- `src/main/resources/messages/messages_es.properties` - Spanish translations; add `lang.*` keys
- `src/main/resources/messages/messages_de.properties` - German translations; add `lang.*` keys
- `src/main/resources/messages/messages_en.properties` - English fallback (empty); no changes needed
- `src/main/resources/messages/messages_fa.properties` - Farsi translations; add `lang.*` keys for sync
- `src/main/resources/messages/messages_ko.properties` - Korean translations; add `lang.*` keys for sync
- `src/main/resources/messages/messages_pt.properties` - Portuguese translations; add `lang.*` keys for sync
- `src/main/resources/messages/messages_ru.properties` - Russian translations; add `lang.*` keys for sync
- `src/main/resources/messages/messages_tr.properties` - Turkish translations; add `lang.*` keys for sync
- `src/main/resources/templates/fragments/layout.html` - Shared layout template; add dropdown to navbar
- `src/test/java/org/springframework/samples/petclinic/system/LanguageSelectorIntegrationTest.java` - New integration test for language selector rendering
- `e2e-tests/tests/features/language-selector.spec.ts` - New Playwright E2E test for language switching and persistence
- `e2e-tests/tests/pages/base-page.ts` - Base page object; add language selector helper methods

### Notes

- Unit tests should be placed in the same package as the code they test (e.g., `system/` package for layout-related tests)
- Use `./mvnw test` to run Java tests
- Use `cd e2e-tests && npm test` to run Playwright E2E tests
- Follow the repository's existing Arrange-Act-Assert test pattern
- New message keys must be added to ALL property files (except `messages_en.properties` which uses fallback) to pass `I18nPropertiesSyncTest`
- Use conventional commit format for all commits

## Tasks

### [x] 1.0 Add i18n message keys for language names

#### 1.0 Proof Artifact(s)

- Test: `I18nPropertiesSyncTest` passes — demonstrates all language files are in sync with new keys
- Diff: New keys `lang.en`, `lang.es`, `lang.de`, `lang.selector` added to all 9 message property files

#### 1.0 Tasks

- [x] 1.1 Write a failing test (RED): run `I18nPropertiesSyncTest` to confirm it currently passes, then add `lang.en`, `lang.es`, `lang.de`, and `lang.selector` keys to `messages.properties` only — the sync test should now fail because other language files are missing the new keys
- [x] 1.2 Make the test pass (GREEN): add the same 4 keys (`lang.en`, `lang.es`, `lang.de`, `lang.selector`) to all non-English locale files (`messages_es.properties`, `messages_de.properties`, `messages_fa.properties`, `messages_ko.properties`, `messages_pt.properties`, `messages_ru.properties`, `messages_tr.properties`). Use native language names: `lang.en=English`, `lang.es=Español`, `lang.de=Deutsch` in all files (language names stay in their native form regardless of locale). `lang.selector` should be translated per locale (e.g., English: "Language", Spanish: "Idioma", German: "Sprache")
- [x] 1.3 Run `./mvnw test -Dtest=I18nPropertiesSyncTest` and verify it passes

### [x] 2.0 Add language selector dropdown to navbar

#### 2.0 Proof Artifact(s)

- Test: `LanguageSelectorIntegrationTest` — MockMvc/SpringBootTest verifying the layout renders a language selector dropdown with three language options and highlights the active language
- Screenshot: Homepage with the language dropdown visible in the header navbar

#### 2.0 Tasks

- [x] 2.1 Write a failing integration test (RED): create `LanguageSelectorIntegrationTest.java` in `src/test/java/org/springframework/samples/petclinic/system/`. Use `@SpringBootTest` with `MockMvc`. Test that a GET to `/` returns HTML containing a language selector element (e.g., an element with `id="languageSelector"`) with three language options. Run the test and confirm it fails
- [x] 2.2 Add the dropdown to `layout.html` (GREEN): in the navbar's `<div class="collapse navbar-collapse" id="main-navbar">`, after the existing `<ul class="nav navbar-nav ms-auto">` block, add a new `<ul>` with a Bootstrap 5 dropdown. The trigger button should show a `fa-globe` icon and the current language name using `th:text`. The dropdown menu should list English, Español, and Deutsch as links. Each link should use `th:href` to append `?lang=xx` to the current URL. Use `th:classappend` to add `active` class to the currently selected language based on `#locale.language`
- [x] 2.3 Run the integration test from 2.1 and verify it passes. Also run `I18nPropertiesSyncTest` and `checkNonInternationalizedStrings` to ensure no regressions

### [x] 3.0 Wire dropdown to locale switching and verify persistence

#### 3.0 Proof Artifact(s)

- Screenshot: Homepage shown in English with dropdown visible
- Screenshot: Same page shown in Spanish after selecting "Español"
- Screenshot: Same page shown in German after selecting "Deutsch"
- Test: Integration test verifying that requesting a page with `?lang=es` returns Spanish-translated content and the dropdown highlights "Español" as active

#### 3.0 Tasks

- [x] 3.1 Write a failing test (RED): add test methods to `LanguageSelectorIntegrationTest.java` that perform GET requests with `?lang=es` and `?lang=de` parameters, then assert the response contains the translated nav labels (e.g., `Inicio` for Spanish home, `Startseite` for German home) and that the active language in the dropdown matches the requested locale. Run and confirm they fail
- [x] 3.2 Make tests pass (GREEN): ensure the dropdown links in `layout.html` correctly generate URLs with `?lang=xx` parameters. The existing `LocaleChangeInterceptor` and `SessionLocaleResolver` handle the rest — verify the Thymeleaf expressions correctly resolve translated text and highlight the active language
- [x] 3.3 Run full test suite (`./mvnw test`) and verify all tests pass with no regressions
- [x] 3.4 Start the app (`./mvnw spring-boot:run`), manually verify the dropdown works in browser, and capture screenshots for proof artifacts. Save screenshots to `docs/specs/02-spec-language-selector/02-proofs/`

### [x] 4.0 Add Playwright E2E tests for language switching and persistence

#### 4.0 Proof Artifact(s)

- Test: Playwright E2E test switches to Spanish, asserts nav labels and heading change, navigates to another page, and asserts the language persists
- Test: Playwright E2E test switches to German and verifies the same persistence behavior

#### 4.0 Tasks

- [x] 4.1 Add language selector helpers to `base-page.ts`: add a method `selectLanguage(lang: string)` that clicks the language dropdown and selects the given language, and a method `currentLanguage()` that returns the currently displayed language name from the dropdown trigger
- [x] 4.2 Write Playwright E2E test (RED): create `e2e-tests/tests/features/language-selector.spec.ts`. Write a test that opens the homepage, switches to Spanish using the dropdown, asserts that the nav label changes (e.g., "Home" → "Inicio"), asserts the page heading changes to Spanish, then navigates to the Veterinarians page and asserts it's still in Spanish. Write a similar test for German. Run `cd e2e-tests && npm test -- --grep "language"` and confirm the tests fail
- [x] 4.3 Make E2E tests pass (GREEN): if tests fail due to selector issues, adjust the selectors or page object methods to match the actual HTML structure from task 2.2. Run the E2E tests again and confirm they pass
- [x] 4.4 Run the full E2E suite (`cd e2e-tests && npm test`) to verify no regressions in other tests
