# Known issues and remaining work

## Corrected during portfolio review

- Purchases use only visible selected IDs. One Service transaction records history and removes the same IDs; failures roll back all participating writes.
- Shopping-list quantity edits validate and save a positive integer. Failed saves restore the row's previous value and report the error.
- History merges only matching date, name, unit and category. A difference in any field keeps records separate.

These corrections passed regression checks; see [verification](VERIFICATION.md).

## Additional corrections

- Catalogue edits reject another item's matching name/unit/category while permitting edits to the item itself.
- History filters combine name/category/date and reload from all records. Both date boundaries are inclusive; clearing a date and Reset restore matching records.
- Replenishment suggestions update the reason on the existing catalogue-item/type row, preserving quantity and rejection state. Related-item and replenishment suggestions remain separate types.
- Import supports CSV only. SHA-256 hashes of successful imports are stored in `imported_files`; renamed files with identical bytes are skipped, including after application restarts. Catalogue, history and hash writes share a transaction.

## Confirmed remaining test issues

Four original GUI failures remain under review: three menu-node queries and repeated JavaFX Toolkit initialization. Complete native mouse and layout acceptance remains unfinished.

## Other source-level findings and review limits

- G_item.equals(null) returns true and hashCode is not overridden.
- Recommendation grouping still uses product names and may conflate catalogue variants.
- The initializer's last-update date is in memory; it does not guarantee once-per-day updates across restarts.
- Entities include JavaFX properties and some controllers still contain substantial business/import logic.
- FXML refers to newer JavaFX API versions than the declared runtime. Startup succeeded with warnings; complete visual compatibility is unverified.
- The buildscript uses dynamic Spring Boot plugin classpath version 2.+. Framework/build upgrades were not included in this correction.
- Some original test classes are empty or have weak assertions. The two purchase-history unit tests now exercise real nonempty input, and new persistence/rollback regressions were added; this is not complete coverage.

## Before publishing

- Complete manual UI workflows, screenshots and a short demonstration.
- Preserve team attribution; exact commit-level ownership still needs original Git history, absent from this snapshot.
- Local environment files remain excluded from Git. The original report is omitted from the public-facing candidate because it contains student identifiers and personal declarations.
- Legacy GitLab pipelines under doc/archive/ci are historical. No passing GitHub Actions pipeline is claimed.

CSV files with changed bytes are treated as new imports, even when their logical rows match. Import hashes from files imported before this correction are unavailable.
