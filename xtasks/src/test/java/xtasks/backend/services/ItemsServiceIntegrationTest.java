package xtasks.backend.services;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import xtasks.backend.entities.L_item;
import xtasks.backend.entities.G_item;
import xtasks.backend.repositories.ItemsRepository;
import xtasks.backend.repositories.GroceryListRepository;
import xtasks.backend.services.ItemsService;
import xtasks.backend.services.exceptions.GenericServiceException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemsServiceIntegrationTest {

    @Autowired
    public ItemsService itemsService;

    @Autowired
    public ItemsRepository itemsRepository;

    @Autowired
    private GroceryListRepository listRepository;

    @Test
    public void itemsReturnEmptyWhenDatabaseIsEmpty() {
        List<G_item> groceryItems = itemsService.items();
        Assertions.assertTrue(groceryItems.isEmpty());
    }

    @Test
    public void itemsShouldReturnCorrectTask() throws GenericServiceException {
        G_item groceryItem = itemsService.create("Gram", "Apple", "", "Fruits");

        List<G_item> items = itemsService.items();
        assertEquals(1, items.size());

        G_item foundGroceryItem = items.get(0);

        assertEquals(groceryItem, foundGroceryItem);
    }

    @Test
    public void createShouldReturnCorrectTask() throws GenericServiceException {
        G_item groceryItem = itemsService.create("Gram", "Apple", "", "Fruits");

        Optional<G_item> foundGroceryItem = itemsRepository.findById(groceryItem.getId());
        assertEquals(groceryItem, foundGroceryItem.get());
    }

    @Test
    public void updateItemShouldUpdateTheItem() throws GenericServiceException{
        G_item groceryItem = itemsService.create("Gram", "Apple", "", "Fruits");

        groceryItem.setUnit("Piece");
        groceryItem.setName("Avocado");
        groceryItem.setCategory("Vegetables");

        itemsService.updateItem(groceryItem.getId(), groceryItem.getUnit(), groceryItem.getName(), groceryItem.getImage(), groceryItem.getCategory());

        Optional<G_item> foundGroceryItem = itemsRepository.findById(groceryItem.getId());
        assertEquals(groceryItem, foundGroceryItem.get());
    }

    @Test
    public void testAddNewItemToList() throws GenericServiceException{
        G_item gItem = itemsService.create("Piece","Mango","","Fruit");
        gItem.setQuantity(5);

        itemsService.addItemToList(gItem);

        List<L_item> lItems = listRepository.findByCatalogueItem(gItem);
        assertFalse(lItems.isEmpty(), "The list should not be empty after adding an item.");
        assertEquals(5, lItems.get(0).getQuantity(), "The quantity of the new item should be 5.");
    }

    @Test
    public void testUpdateExistingItemQuantity() throws GenericServiceException{
        G_item gItem = itemsService.create("Piece","Mango","","Fruit");
        gItem.setQuantity(5);

        itemsService.addItemToList(gItem);
        itemsService.addItemToList(gItem);

        List<L_item> lItems = listRepository.findByCatalogueItem(gItem);
        assertEquals(1, lItems.size(), "There should be only one item in the list.");
        assertEquals(10, lItems.get(0).getQuantity(), "The quantity of the item should be updated to 10.");
    }

}
