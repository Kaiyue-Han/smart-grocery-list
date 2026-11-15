# Verification after correctness and import corrections

Date: 2026-11-15. Windows 11, Temurin JDK 11.0.33+10, Gradle Wrapper 7.6.

| Check | Result |
| --- | --- |
| Compilation | Passed |
| Backend tests, including persistence and rollback regressions | 42 passed, 0 failed |
| Full repository suite | 50 reported, 46 passed, 4 failed |
| Real MariaDB functional acceptance | 25 passed, 0 failed |
| Main xTASKS database | Counts unchanged; acceptance used an isolated database |
| Original team sources | Unchanged; corrections are in the review copy |

The four remaining original UI failures are three TestFX interactions unable to locate a visible Bakery node, plus repeated JavaFX Toolkit initialization. The suite is not fully passing.

## Functional checks

Acceptance invokes production Spring services and FXML-initialized JavaFX controller handlers against an isolated real MariaDB schema. It covers catalogue validation/editing/search, shopping-list accumulation and persisted quantity editing, visible-only selected purchase, four-field history merging, date-range narrowing/widening/clearing/reset and combined search, CSV import, repeated-content skip, JSON rejection, purchase-cycle/related-item suggestions and deduplication. A forced MariaDB deletion failure verifies rollback of the whole purchase workflow.

Six additional H2 regression tests verify catalogue duplicate edit rejection, suggestion state preservation, separate recommendation types/catalogue variants, renamed identical CSV skipping, failed-import rollback and retry, and invalid/unsupported file rejection. The controller import test now asserts success, duplicate and error feedback.

This is not complete native mouse-click or layout acceptance. Recommendation accuracy and every edge case are not established by these checks.

## Test environment adaptations

Repository tests use H2. The existing Byte Buddy agent was preloaded through JAVA_TOOL_OPTIONS to avoid sandbox dynamic-attachment limits. JavaFX uses a process-local workspace user.home for writable native caches. FXML/runtime version warnings remain. No framework or build dependency upgrade was included.

The original external baseline had 13 passes, 8 failed expectations and 1 observation in 22 scenarios. The CSV expectations were then changed according to the owner's decision: CSV only, and skip identical successfully imported file contents. Three more checks cover date reset/upper-bound behavior and transaction rollback. The latest 25 checks pass.

See [known issues](KNOWN_ISSUES.md) for remaining source-level findings and publication prerequisites.

## Visual walkthrough

The [37-second scripted demo and screenshots](DEMO.md) use the production application and an isolated MariaDB database. Capture-specific assertions verified hidden Bread preservation, Apple purchase quantity 2 and identical CSV quantity 3 after repeat import. Main database counts were unchanged. MP4 was fully decoded successfully; GIF duration is 37 seconds. Captured UI frames were inspected. The walkthrough supplies visual evidence, not additional passing TestFX tests or full accessibility acceptance.
