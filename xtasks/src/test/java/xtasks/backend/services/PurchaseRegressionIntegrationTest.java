package xtasks.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import xtasks.backend.entities.*;
import xtasks.backend.repositories.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:purchase_regression;DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PurchaseRegressionIntegrationTest {
    @Autowired private GroceryListService listService;
    @Autowired private PastPurchasesService historyService;
    @Autowired private ItemsService itemsService;
    @Autowired private ItemsRepository catalogue;
    @Autowired private PastPurchasesListRepository history;
    @Autowired private SuggestionRepository suggestions;
    @SpyBean private GroceryListRepository list;

    private static final LocalDate DATE = LocalDate.of(2026, 11, 15);

    @BeforeEach
    void cleanIsolatedTestDatabase() {
        reset(list);
        suggestions.deleteAllInBatch();
        list.deleteAllInBatch();
        history.deleteAll();
        catalogue.deleteAllInBatch();
    }

    private L_item addToList(String name, int quantity) throws Exception {
        G_item item = itemsService.create("Piece", name, "", "Fruits");
        item.setQuantity(quantity);
        itemsService.addItemToList(item);
        return list.findByCatalogueItem(item).get(0);
    }

    @Test
    void purchaseShouldPreserveAnUnrequestedHiddenItem() throws Exception {
        L_item visible = addToList("Visible apple", 2);
        L_item hidden = addToList("Hidden pear", 3);
        Set<Long> purchased = listService.purchaseSelectedItems(Collections.singleton(visible.getId()));
        assertEquals(Collections.singleton(visible.getId()), purchased);
        assertFalse(list.existsById(visible.getId()));
        assertTrue(list.existsById(hidden.getId()));
        assertTrue(history.findByName("Hidden pear").isEmpty());
        P_item recorded = history.findByName("Visible apple").get(0);
        assertEquals(2, recorded.getQuantity());
        assertEquals(LocalDate.now(), recorded.getAddDate());
    }

    @Test
    void editedQuantityShouldSurviveAFreshDatabaseReadAndPurchase() throws Exception {
        L_item item = addToList("Quantity apple", 2);
        listService.updateQuantity(item.getId(), 7);
        assertEquals(7, list.findById(item.getId()).get().getQuantity());
        listService.purchaseSelectedItems(Collections.singleton(item.getId()));
        assertEquals(7, history.findByName("Quantity apple").get(0).getQuantity());
    }

    @Test
    void failedQuantitySaveShouldRollbackTheDatabaseValue() throws Exception {
        L_item item = addToList("Save failure apple", 2);
        doThrow(new IllegalStateException("simulated save failure")).when(list).save(any(L_item.class));
        assertThrows(IllegalStateException.class, () -> listService.updateQuantity(item.getId(), 7));
        assertEquals(2, list.findById(item.getId()).get().getQuantity());
    }

    @Test
    void failedListDeletionShouldRollbackHistoryAndRejectedSuggestionCleanup() throws Exception {
        L_item item = addToList("Rollback apple", 2);
        S_item rejected = new S_item(item.getCatalogueItem(), 1, "test reason");
        rejected.setIsReject(true);
        rejected = suggestions.save(rejected);
        Long suggestionId = rejected.getId();
        doThrow(new IllegalStateException("simulated deletion failure")).when(list).deleteByListItemIdIn(anySet());
        assertThrows(IllegalStateException.class, () -> listService.purchaseSelectedItems(Collections.singleton(item.getId())));
        assertTrue(list.existsById(item.getId()));
        assertTrue(history.findByName("Rollback apple").isEmpty());
        assertTrue(suggestions.existsById(suggestionId));
    }

    @Test
    void emptyPurchaseShouldNotClearRejectedSuggestions() throws Exception {
        L_item item = addToList("Unselected apple", 2);
        S_item rejected = new S_item(item.getCatalogueItem(), 1, "test reason");
        rejected.setIsReject(true);
        rejected = suggestions.save(rejected);
        assertTrue(listService.purchaseSelectedItems(Collections.emptySet()).isEmpty());
        assertTrue(list.existsById(item.getId()));
        assertTrue(suggestions.existsById(rejected.getId()));
        assertTrue(history.findAll().isEmpty());
    }

    @Test
    void exactDateNameUnitAndCategoryShouldMergeQuantities() {
        historyService.addItemsToPastPurchases(Arrays.asList(
                new P_item("Rice", "Gram", "Cereals", 2, DATE),
                new P_item("Rice", "Gram", "Cereals", 3, DATE)));
        assertEquals(1, history.findAll().size());
        assertEquals(5, history.findAll().get(0).getQuantity());
    }

    @Test
    void changingAnyOneMergeFieldShouldKeepHistorySeparate() {
        historyService.addItemsToPastPurchases(Arrays.asList(
                new P_item("Apple", "Piece", "Fruits", 2, DATE),
                new P_item("Apple", "Gram", "Fruits", 100, DATE),
                new P_item("Apple", "Piece", "Other", 3, DATE),
                new P_item("Apple", "Piece", "Fruits", 4, DATE.plusDays(1)),
                new P_item("Pear", "Piece", "Fruits", 5, DATE)));
        assertEquals(5, history.findAll().size());
        P_item original = history.findByAddDateAndNameAndUnitAndCategory(DATE, "Apple", "Piece", "Fruits").get();
        assertEquals(2, original.getQuantity());
        assertEquals(100, history.findByAddDateAndNameAndUnitAndCategory(DATE, "Apple", "Gram", "Fruits").get().getQuantity());
    }
}
