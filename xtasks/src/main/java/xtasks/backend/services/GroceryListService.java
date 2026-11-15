package xtasks.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtasks.backend.entities.G_item;
import xtasks.backend.entities.L_item;
import xtasks.backend.entities.P_item;
import xtasks.backend.repositories.GroceryListRepository;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class GroceryListService {
    @Autowired
    private GroceryListRepository listRepository;

    @Autowired
    private PastPurchasesService pastPurchasesService;

    /** Saves a validated quantity independently of the JavaFX table's in-memory row. */
    @Transactional
    public void updateQuantity(Long id, Integer quantity) {
        if (id == null || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive whole number.");
        }
        L_item item = listRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("The shopping list item no longer exists."));
        item.setQuantity(quantity);
        listRepository.save(item);
    }

    /** Records and removes exactly the IDs supplied by the visible-selection controller. */
    @Transactional
    public Set<Long> purchaseSelectedItems(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptySet();
        }
        List<L_item> purchasedItems = listRepository.findAllById(ids);
        if (purchasedItems.isEmpty()) {
            return Collections.emptySet();
        }
        LocalDate purchaseDate = LocalDate.now();
        List<P_item> purchases = purchasedItems.stream().map(item -> {
            G_item catalogueItem = item.getCatalogueItem();
            return new P_item(catalogueItem.getName(), catalogueItem.getUnit(),
                    catalogueItem.getCategory(), item.getQuantity(), purchaseDate);
        }).collect(Collectors.toList());
        Set<Long> purchasedIds = purchasedItems.stream()
                .map(L_item::getId).collect(Collectors.toSet());
        pastPurchasesService.addItemsToPastPurchases(purchases);
        listRepository.deleteByListItemIdIn(purchasedIds);
        return purchasedIds;
    }

    public List<L_item> L_items(){
        return listRepository.findAll();
    }

    @Transactional
    public void deleteSelectedItems(Set<Long> ids) {
        System.out.println("Deleting items with ids: " + ids);
        listRepository.deleteByListItemIdIn(ids);
    }

    public void deleteAllItems() {
        listRepository.deleteAll();
    }

    public void create(L_item item){
        listRepository.save(item);
    }

    public Optional<L_item> getItembyid(Long id){

        return listRepository.findById(id);
    }
    @Transactional
    public void delete(L_item item){

        listRepository.delete(item);
    }
}
