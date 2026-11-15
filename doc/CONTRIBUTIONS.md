# Team attribution and personal contribution

This repository originated as Team A coursework for SWT-SWL-B, winter semester 2023/24, University of Bamberg. Original team members listed in the report: Mohamed Aziz Keskes, Adrian Alegre Diaz, Di Bao, Suela Çarkaxhija, Mostafa Mohamed Adly Ahmed and Kaiyue Han. Preserve collaborative attribution and the original GPL license.

## Kaiyue Han's contribution

The portfolio owner has identified herself as Kaiyue Han. The following responsibilities are supported by the original team report. Printed page numbers refer to that report; code locations are corresponding implementation areas, not verified Git authorship.

| Reported contribution | Report evidence | Corresponding implementation |
| --- | --- | --- |
| L_item and P_item entities | Page 8 | `backend/entities/L_item.java`, `backend/entities/P_item.java` |
| Selection-based purchase and removal workflows | Pages 8 and 47 | `GroceryListViewController`, `GroceryListService`, `PastPurchasesService` |
| Adding catalogue items to the shopping list, with Di Bao | Pages 39 and 42 | `ItemsService.addItemToList`, catalogue add-button handler |
| Purchase List UI participation | Page 8 | List UI/controllers; exact whole-file ownership remains unverified |
| Suggestion business behavior and integration | Pages 8, 51, 53 and 55 | `SuggestionsService`, suggestion accept/reject/refresh handlers in `GroceryCatalogViewController` |
| Selected tests, QA and ER modeling | Pages 8, 32 and 57-60 | Report ER diagram; page-59 test example corresponds to `ItemsServicePart2UnitTest` |
| Sprint 4 Scrum Master and documentation | Pages 8 and 49-51; QA on page 57 and lessons learned on page 68 | Report sections and team process |

Page 8 distinguishes suggestion-system infrastructure from the related-item/Apriori algorithm attributed to Di Bao. Sprint 5 describes integration as joint work. Do not claim sole ownership of Apriori, all recommendation code or the purchase-cycle calculation without further evidence. Other members' signed report subsections retain their attribution.

Suggested statement: In a six-person university team, I contributed shopping-list and purchase-history entities and workflows, including catalogue-to-list integration, selection-based purchase/removal and suggestion-system integration. I also contributed selected tests and ER modeling and served as Scrum Master for Sprint 4. Apriori-based recommendations were integrated collaboratively; I do not claim sole implementation of that algorithm.

## Git history status

The uploaded snapshot has no `.git` directory. A read-only anonymous request to the original university GitLab required authentication, so no commit IDs, blame or exact author history have been retrieved. Recover an authorized clone containing Git history before making commit-level authorship statements.

Distinguish original coursework contributions from the later portfolio cleanup. Avoid invented user counts, performance improvements, coverage figures or deployment claims.

## Later portfolio maintenance

The visible-selection purchase transaction, quantity persistence and four-field history merge corrections were made during this portfolio review. They are separate from the original 2023/24 coursework contributions described above. New regression tests cover persistence, selection boundaries, matching fields and rollback.
