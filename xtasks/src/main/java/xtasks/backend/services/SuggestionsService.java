package xtasks.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtasks.backend.entities.G_item;
import xtasks.backend.entities.L_item;
import xtasks.backend.entities.P_item;
import xtasks.backend.entities.S_item;
import xtasks.backend.repositories.ItemsRepository;
import xtasks.backend.repositories.GroceryListRepository;
import xtasks.backend.repositories.PastPurchasesListRepository;
import xtasks.backend.repositories.SuggestionRepository;
import xtasks.backend.services.AprioriService;


import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SuggestionsService {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private GroceryListRepository listRepository;

    @Autowired
    private ItemsRepository itemsRepository;

    @Autowired
    private AprioriService aprioriService;

    @Autowired
    private final PastPurchasesListRepository pastPurchasesListRepository;

    public List<S_item> suggestions() {
        return suggestionRepository.findAll();
    }

    public SuggestionsService(PastPurchasesListRepository repository) {
        this.pastPurchasesListRepository = repository;
    }

    @Transactional
    public Map<String, Integer> calculateAveragePurchaseCycles() {
        List<P_item> allPurchases = pastPurchasesListRepository.findAll();
        if (allPurchases.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<LocalDate>> itemPurchaseDates = new HashMap<>();

        for (P_item purchase : allPurchases) {
            String itemName = purchase.getName();
            itemPurchaseDates.computeIfAbsent(itemName, k -> new ArrayList<>()).add(purchase.getAddDate());
        }

        Map<String, Integer> averageCycles = new HashMap<>();

        for (String itemName : itemPurchaseDates.keySet()) {
            List<LocalDate> dates = itemPurchaseDates.get(itemName);
            if (dates == null || dates.isEmpty()) {
                continue;
            }
            dates.sort(LocalDate::compareTo);

            List<Long> cycles = new ArrayList<>();
            for (int i = 1; i < dates.size(); i++) {
                long daysBetween = ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i));
                cycles.add(daysBetween);
            }

            // remove min and max, if the size is bigger than 5
            if (cycles.size() > 5) {
                cycles.remove(Collections.max(cycles));
                cycles.remove(Collections.min(cycles));
            }

            double average = cycles.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(Double.NaN);

            if (!Double.isNaN(average)) {
                averageCycles.put(itemName, (int) Math.round(average));
            }
        }

        return averageCycles;
    }
    @Transactional
    public Map<String, Long> generateRecommendations() {
        Map<String, Integer> averageCycles = calculateAveragePurchaseCycles();
        Map<String, Long> recommendations = new HashMap<>();
        LocalDate today = LocalDate.now();

        for (Map.Entry<String, Integer> entry : averageCycles.entrySet()) {
            String itemName = entry.getKey();
            int averageCycle = entry.getValue();
            LocalDate lastPurchaseDate = getLastPurchaseDate(itemName);
            if (lastPurchaseDate != null) {
                LocalDate nextExpectedPurchaseDate = lastPurchaseDate.plusDays(averageCycle);
                if (today.equals(nextExpectedPurchaseDate) || today.isAfter(nextExpectedPurchaseDate)) {
                    long daysSinceLastPurchase = ChronoUnit.DAYS.between(lastPurchaseDate, today);
                    recommendations.put(itemName, daysSinceLastPurchase);
                }
            }
        }
        return recommendations;
    }

    /**
     * Creates new suggestions for regularly purchased items based on how long ago they were last purchased.
     * @param nameMap A map of item names to the number of days since the item was last purchased. Used to generate suggestions.
     */
    @Transactional
    public void createSItems(Map<String, Long> nameMap) {
        List<G_item> catalogueItems = itemsRepository.findByNameIn(nameMap.keySet());

        for (G_item catalogueItem : catalogueItems) {
            long daysSinceLastPurchase = nameMap.getOrDefault(catalogueItem.getName(), 0L);
            String reason = "You haven't purchased this item in " + daysSinceLastPurchase + " days.";
            saveSuggestion(catalogueItem, reason, true);
        }
    }

    /**
     * Creates a suggestion based on a related item when an item is added to the list.
     * This method should be called when an item is added to the list, and it generates a suggestion
     * for the provided related item's name. It checks if a suggestion already exists for each matching item
     * in the catalogue and creates a new suggestion if none exists.
     *
     * @param relatedItemName The name of the related item for which a suggestion is generated.
     */
    @Transactional
    public void createSItems(String relatedItemName) {
        List<G_item> catalogueItems = itemsRepository.findByName(relatedItemName);
        String reason = "You have added a related item.";

        for (G_item catalogueItem : catalogueItems) {
            saveSuggestion(catalogueItem, reason, false);
        }
    }

    private void saveSuggestion(G_item item, String reason, boolean regular) {
        if (listRepository.existsByCatalogueItem(item)) return;
        for (S_item existing : suggestionRepository.findByCatalogueItem(item)) {
            String previous = existing.getReason();
            boolean sameType = regular
                    ? previous != null && previous.startsWith("You haven't purchased this item in ")
                    : reason.equals(previous);
            if (sameType) {
                existing.setReason(reason);
                suggestionRepository.save(existing);
                return;
            }
        }
        suggestionRepository.save(new S_item(item, item.getDefaultQuantity(), reason));
        incrementNewSuggestionsCount();
    }

    /**
     * Initializes and updates daily recommendations for regularly purchased items,
     * tracking updates within the current application process. This maintains
     * current suggestions without unnecessary repetitions.
     */
    @Component
    public class DataInitializer {
        // Stores the date when the data was last updated.
        private LocalDate lastUpdateDate = null;

        @PostConstruct
        public void init() {
            updateIfNeeded();
        }

        /**
         * Updates recommendations daily, if not already updated today, by generating new suggestions
         * and refreshing system items. Ensures daily refresh without repetition.
         */
        public void updateIfNeeded() {
            LocalDate today = LocalDate.now();
            if (lastUpdateDate == null || !lastUpdateDate.equals(today)) {
                Map<String, Long> recommendations = generateRecommendations();
                createSItems(recommendations);
                lastUpdateDate = today;
            }
        }
    }

    /**
     * Manages the count of new suggestions.
     */
    private AtomicInteger newSuggestionsCount = new AtomicInteger(0);

    /**
     * Increments the count of new suggestions.
     */
    public void incrementNewSuggestionsCount() {
        newSuggestionsCount.incrementAndGet();
    }

    /**
     * Resets the count of new suggestions to zero.
     */
    public void resetNewSuggestionsCount() {
        newSuggestionsCount.set(0);
    }

    /**
     * Retrieves the current count of new suggestions.
     * @return The count of new suggestions.
     */
    public int getNewSuggestionsCount() {
        return newSuggestionsCount.get();
    }

    private LocalDate getLastPurchaseDate(String itemName) {
        List<P_item> purchases = pastPurchasesListRepository.findByName(itemName);
        if (purchases == null || purchases.isEmpty()) {
            return null;
        }

        return purchases
                .stream()
                .map(P_item::getAddDate) // Assuming P_item has getPurchaseDate method
                .max(LocalDate::compareTo)
                .orElse(null); // Returns null if there are no purchase records
    }

    public void delete(S_item suggestion){
        suggestionRepository.delete(suggestion);
    }

    /**
     * Adds an item to the shopping list or updates its quantity if already present.
     *
     * @param suggestion The suggestion item to be added or updated.
     */
    public void addItemToList(S_item suggestion) {
        // SQL query to insert the item into the cart table
        List<L_item> lItems = listRepository.findByCatalogueItem(suggestion.getCatalogueItem());
        // If the item is not in the shopping list, create a new entry.
        if (lItems.isEmpty()) {
            //System.out.println("to be added to shopping list"+suggestion+suggestion.getQuantity());
            L_item newLItem = new L_item(suggestion.getCatalogueItem(),suggestion.getQuantity());
            listRepository.save(newLItem);
        } else {
            // If the item is already in the shopping list, update its quantity.
            //System.out.println("to be added to shopping list"+suggestion+suggestion.getQuantity());
            L_item lItem = lItems.get(0);
            lItem.setQuantity(lItem.getQuantity() + suggestion.getQuantity());
            listRepository.save(lItem);
        }
    }

    /**
     * Adds all suggestions from the repository to the shopping list.
     */
    @Transactional
    public void addAllSuggestionsToList() {
        List<S_item> suggestions = suggestionRepository.findAll();
        for (S_item suggestion : suggestions) {
            addItemToList(suggestion);
        }
    }

    /**
     * Updates a suggestion item in the repository.
     *
     * @param sItem The suggestion item to be updated.
     */
    public void updateSItem(S_item sItem) {
        suggestionRepository.save(sItem);
    }

}
    
