package xtasks.backend.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import xtasks.backend.entities.*;
import xtasks.backend.repositories.*;
import xtasks.backend.services.exceptions.GenericServiceException;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:followup_regression;DB_CLOSE_DELAY=-1")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FollowupRegressionIntegrationTest {
    @Autowired ItemsService items;
    @Autowired SuggestionsService service;
    @Autowired PurchaseImportService importer;
    @Autowired ItemsRepository catalogue;
    @Autowired SuggestionRepository suggestions;
    @Autowired GroceryListRepository list;
    @Autowired ImportedFileRepository imported;
    @SpyBean PastPurchasesListRepository history;
    @TempDir Path temp;

    @BeforeEach void clean() {
        reset(history);
        suggestions.deleteAllInBatch(); list.deleteAllInBatch(); history.deleteAll();
        catalogue.deleteAllInBatch(); imported.deleteAllInBatch();
    }
    G_item product(String name) throws Exception { return items.create("Piece",name,"","Bakery"); }
    Path csv(String name) throws Exception {
        return Files.writeString(temp.resolve(name),"name;date;quantity;unit;category;\nApple;2026-11-15;4;Piece;Bakery;\n");
    }
    @Test void duplicateEditShouldLeaveOriginalUntouched() throws Exception {
        G_item a=product("Apple"), b=product("Pear");
        assertThrows(GenericServiceException.class,()->items.updateItem(b.getId(),"Piece","Apple","changed","Bakery"));
        assertEquals("Pear",catalogue.findById(b.getId()).get().getName());
        assertEquals("",catalogue.findById(b.getId()).get().getImage());
        items.updateItem(a.getId(),"Piece","Apple",null,"Bakery");
        assertEquals("",catalogue.findById(a.getId()).get().getImage());
        items.updateItem(b.getId(),"Kg","Apple","","Bakery");
        assertEquals(2,catalogue.findByName("Apple").size());
    }
    @Test void dailyRefreshShouldPreserveQuantityAndRejection() throws Exception {
        G_item a=product("Apple"); service.createSItems(Collections.singletonMap("Apple",10L));
        S_item row=suggestions.findByCatalogueItem(a).get(0); row.setQuantity(8); row.setIsReject(true); suggestions.save(row);
        service.resetNewSuggestionsCount(); service.createSItems(Collections.singletonMap("Apple",11L));
        List<S_item> result=suggestions.findByCatalogueItem(a);
        assertEquals(1,result.size()); assertEquals(row.getId(),result.get(0).getId());
        assertEquals(8,result.get(0).getQuantity()); assertTrue(result.get(0).getIsReject());
        assertTrue(result.get(0).getReason().contains("11 days")); assertEquals(0,service.getNewSuggestionsCount());
    }
    @Test void recommendationTypesAndVariantsShouldRemainSeparate() throws Exception {
        G_item a=product("Apple"), b=items.create("Kg","Apple","","Bakery");
        service.createSItems("Apple"); service.createSItems("Apple");
        service.createSItems(Collections.singletonMap("Apple",10L));
        assertEquals(2,suggestions.findByCatalogueItem(a).size());
        assertEquals(2,suggestions.findByCatalogueItem(b).size());
    }
    @Test void sameBytesRenamedShouldBeSkippedAcrossServiceCalls() throws Exception {
        Path file=csv("first.csv"); assertTrue(importer.importCsv(file.toFile()));
        Path renamed=Files.copy(file,temp.resolve("renamed.csv"));
        assertFalse(importer.importCsv(renamed.toFile()));
        assertEquals(1,imported.count()); assertEquals(4,history.findByName("Apple").get(0).getQuantity());
    }
    @Test void failedImportShouldRollbackLedgerAndCatalogueThenAllowRetry() throws Exception {
        Path file=csv("retry.csv");
        doThrow(new IllegalStateException("injected database failure")).when(history).save(any(P_item.class));
        assertThrows(IllegalStateException.class,()->importer.importCsv(file.toFile()));
        assertEquals(0,imported.count()); assertEquals(0,catalogue.count()); assertEquals(0,history.count());
        reset(history); assertTrue(importer.importCsv(file.toFile())); assertEquals(1,history.count());
    }
    @Test void unsupportedOrInvalidFilesShouldNotWriteData() throws Exception {
        Path json=Files.writeString(temp.resolve("file.json"),"[]");
        assertThrows(IllegalArgumentException.class,()->importer.importCsv(json.toFile()));
        Path invalid=Files.writeString(temp.resolve("bad.csv"),"name;date;quantity;unit;category;\nApple;2026-11-15;0;Piece;Bakery;\n");
        assertThrows(IllegalArgumentException.class,()->importer.importCsv(invalid.toFile()));
        assertEquals(0,imported.count()); assertEquals(0,history.count()); assertEquals(0,catalogue.count());
    }
}
