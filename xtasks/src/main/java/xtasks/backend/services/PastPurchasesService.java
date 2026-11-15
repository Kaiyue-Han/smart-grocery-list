package xtasks.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtasks.backend.entities.L_item;
import xtasks.backend.entities.P_item;
import xtasks.backend.repositories.PastPurchasesListRepository;
import xtasks.backend.repositories.SuggestionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PastPurchasesService {
    @Autowired
    private PastPurchasesListRepository pastPurchasesListRepository;
    @Autowired
    private SuggestionRepository suggestionRepository;

    public List<P_item> P_items(){
        return pastPurchasesListRepository.findAll();
    }

    @Transactional
    public void addItemsToPastPurchases(List<P_item> items) {
        for (P_item newItem : items) {
            // Merge only records with the same date, name, unit and category.
            Optional<P_item> existingItemOpt = pastPurchasesListRepository
                    .findByAddDateAndNameAndUnitAndCategory(newItem.getAddDate(),
                            newItem.getName(), newItem.getUnit(), newItem.getCategory());

            if (existingItemOpt.isPresent()) {
                // if yes, add up the quantity.
                P_item existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
                pastPurchasesListRepository.save(existingItem);
            } else {
                // if no, then create new item in past purchase list.
                pastPurchasesListRepository.save(newItem);
            }
        }
        //After completing the addition operations, delete all S_item records where isReject is true
        suggestionRepository.deleteAllByIsReject(true);
    }
    public void delete(P_item item){
        pastPurchasesListRepository.delete(item);
    }
}
