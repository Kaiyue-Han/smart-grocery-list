package xtasks.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtasks.backend.entities.G_item;
import xtasks.backend.entities.L_item;
import xtasks.backend.repositories.GroceryListRepository;
import xtasks.backend.repositories.ItemsRepository;
import xtasks.backend.services.exceptions.GenericServiceException;

import java.util.List;
import java.util.Optional;

@Service
public class ItemsService {

    @Autowired
    public ItemsRepository itemsRepository;

    @Autowired
    public GroceryListRepository listRepository;

    /**
     *
     * Validates the grocery item unit
     *
     * @param unit
     * @throws GenericServiceException if the unit is equal to null or a unit is not selected.
     */
    public void validateUnit(String unit) throws GenericServiceException {
        if (unit == null) {
            throw new GenericServiceException("The passed unit shall not be null");
        }

        if (unit.equals("Unit")) {
            throw new GenericServiceException("You have to select a unit. ");
        }
    }

    /**
     *
     * Validates the grocery item name
     *
     * @param name
     * @throws GenericServiceException if the title is null, empty, or blank.
     */
    public void validateName(String name) throws GenericServiceException {
        if (name == null) {
            throw new GenericServiceException("The passed name shall not be null");
        }

        if (name.isEmpty()) {
            throw new GenericServiceException("The passed name shall not be empty.");
        }

        if (name.isBlank()) {
            throw new GenericServiceException("The passed name shall not be blank.");
        }

    }

    /**
     *
     * Validates the grocery item image.
     *
     * @param image
     * @return a default image if the passed image is null. Otherwise, return the same image.
     */
    public String validateImage(String image) {
        if (image == null) {
            return ""; // Default image here.
        }

        return image;
    }

    /**
     *
     * Validates the grocery item category.
     *
     * @param category
     * @throws GenericServiceException if the category is null or category was not selected.
     */
    public void validateCategory(String category) throws GenericServiceException {
        if (category == null) {
            throw new GenericServiceException("The passed category shall not be null");
        }

        if (category.equals("Category")) {
            throw new GenericServiceException("You have to select a category. ");
        }
    }

    /**
     *
     * Creates a new G_item object and persists it in the database.
     *
     * @param unit
     * @param name
     * @param image
     * @param category
     * @throws GenericServiceException if the validation of any parameter provided falls.
     */
    public G_item create(String unit, String name, String image, String category) throws GenericServiceException {
        validateCategory(category);
        validateName(name);
        validateUnit(unit);
        image = validateImage(image);

        validateItemDoesNotExists(name, category, unit);

        G_item newGroceryItem = new G_item(unit, category, image, name);

        newGroceryItem.setQuantity(newGroceryItem.getDefaultQuantity());
        itemsRepository.save(newGroceryItem);

        return newGroceryItem;
    }

    /**
     *
     * @param name
     * @param category
     * @param unit
     * @throws GenericServiceException if the name, category and unit passed are the same
     */
    public void validateItemDoesNotExists(String name, String category, String unit) throws GenericServiceException {
        validateItemDoesNotExistExcept(name, category, unit, null);
    }

    private void validateItemDoesNotExistExcept(String name, String category, String unit, Long excludedId) throws GenericServiceException {
        List<G_item> groceryItems = itemsRepository.findByName(name);

        if (groceryItems.isEmpty()) {
            return;
        }

        for (G_item groceryItem : groceryItems) {
            if ((excludedId == null || !java.util.Objects.equals(groceryItem.getId(), excludedId)) && groceryItem.getCategory().equals(category) && groceryItem.getUnit().equals(unit)) {
                throw new GenericServiceException("The item already exists. ");
            }
        }
    }

    /**
     *
     * Updates the attributes of a G_item object and save the changes in the database.
     *
     * @param id
     * @param unit
     * @param name
     * @param image
     * @param category
     * @throws GenericServiceException
     */

    @Transactional(rollbackFor = GenericServiceException.class)
    public void updateItem(long id, String unit, String name, String image, String category) throws GenericServiceException {
        validateCategory(category);
        validateName(name);
        validateUnit(unit);
        image = validateImage(image);
        G_item item = itemsRepository.findById(id).orElseThrow(() -> new GenericServiceException("The item no longer exists."));
        validateItemDoesNotExistExcept(name, category, unit, id);
        item.setUnit(unit);
        item.setName(name);
        item.setCategory(category);
        item.setImage(image);

        itemsRepository.save(item);
    }

    /**
     *
     * @return a list of all grocery items.
     */
    public List<G_item> items() {
        return itemsRepository.findAll();
    }

    /**
     *
     * Add a G_item to the grocery list and persist it in the database.
     *
     * @param gItem
     */
    public void addItemToList(G_item gItem) {
        List<L_item> lItems = listRepository.findByCatalogueItem(gItem);
        if (lItems.isEmpty()) {
            L_item newLItem = new L_item(gItem,gItem.getQuantity());
            try {
                listRepository.save(newLItem);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            L_item lItem = lItems.get(0);
            lItem.setQuantity(lItem.getQuantity() + gItem.getQuantity());
            listRepository.save(lItem);
        }
    }

    public void delete(G_item item){
        itemsRepository.delete(item);
    }
}


