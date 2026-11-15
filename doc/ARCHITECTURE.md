# Architecture grounded in the checked-in code

## Startup and UI

`Main.main` delegates to `App.main`. `App` extends JavaFX `Application`, starts Spring in `init`, and uses `springContext::getBean` as the FXML controller factory. `mainView.fxml` includes catalogue, shopping list and purchase-history views. This is an in-process desktop system, not a split HTTP frontend/backend deployment.

## Data and services

| Entity | Table | Role |
| --- | --- | --- |
| G_item | items | Catalogue item, category, unit, image; JavaFX quantity property |
| L_item | See entity mapping | Shopping-list item linked to a catalogue item |
| P_item | past_purchases_list | Purchase snapshot with name, quantity, unit, category and date |
| ImportedFile | imported_files | SHA-256 primary key for successful CSV imports |
| S_item | See entity mapping | Suggested catalogue item, quantity, reason and rejection state |

Repositories extend Spring Data JPA. `ItemsService` validates catalogue values and adds items to the shopping list. `GroceryListService` handles shopping-list persistence. `PastPurchasesService` merges purchase quantities only when date, name, unit and category match and clears rejected suggestions after adding purchase history.

## Purchase flow

`GroceryListViewController.addToPastPurchaseList` intersects visible table rows with the selected IDs, then calls `GroceryListService.purchaseSelectedItems`. That transactional Service reads persisted quantities, constructs purchase snapshots, adds history and removes exactly those purchased IDs. `PastPurchasesService` joins the same transaction. A failure in deletion rolls back history and rejected-suggestion cleanup as well. Hidden selections are preserved; purchased selections are removed.

Quantity edits call `GroceryListService.updateQuantity` and update the displayed object after successful persistence. Invalid values or database failures restore the prior row value and display an error. Reloading preserves the active shopping-list filters.

## History filtering and CSV import

History search, category and inclusive date boundaries are applied together to a fresh complete history list. Reset clears all filter controls.

`PurchaseImportService` accepts UTF-8 semicolon CSV (`name;date;quantity;unit;category`, date `yyyy-MM-dd`), validates the entire input, and uses a persisted SHA-256 content hash to skip repeated successful imports. Catalogue variants are matched by name/unit/category. The hash, catalogue additions and history changes commit together or roll back together. The `imported_files` table is created by the existing Hibernate `ddl-auto: update` configuration; retain it with database backups to retain import history.

## Recommendation behavior

`SuggestionsService.calculateAveragePurchaseCycles` groups purchase dates by item name, sorts them, and averages adjacent intervals. If more than five intervals exist, one minimum and one maximum interval are removed. The mean is rounded to whole days. A product becomes due when today is at or beyond last purchase plus that interval. Suggestions use catalogue item identity and recommendation type when checking existing entries. A new day count updates the existing reason, preserving quantity/rejection; items already on the shopping list are skipped. Related-item and replenishment suggestions remain separate.

`AprioriService` constructs baskets by purchase date, restricted to dates on which the input item was purchased. It generates frequent sets with threshold 0.5 and recommends other members of sets containing the input item. The denominator is this filtered basket collection, not every purchase date. Do not describe this as measured predictive accuracy or as a complete confidence/lift association-rule pipeline.

## Reading order for interviews

1. `Main`, `App`, `mainView.fxml`: startup, JavaFX lifecycle and dependency injection.
2. Four entities and repositories: persistence and relationships.
3. `ItemsService`, `GroceryListService`, `PastPurchasesService`: validation and state transitions.
4. Shopping-list controller purchase method: UI-to-database workflow and transactions.
5. `SuggestionsService`, `AprioriService`: algorithms, thresholds and limitations.
6. Tests: distinguish mock unit tests, Spring/H2 integration tests and TestFX UI tests.
