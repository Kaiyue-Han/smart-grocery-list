package xtasks.backend.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xtasks.backend.entities.G_item;
import xtasks.backend.repositories.PastPurchasesListRepository;
import xtasks.backend.entities.P_item;

import java.time.LocalDate;
import java.util.*;

@Service
public class AprioriService {
    @Autowired
    private final PastPurchasesListRepository pastPurchasesListRepository;

    private static final double minSupport = 0.5;

    public AprioriService(PastPurchasesListRepository repository) {
        this.pastPurchasesListRepository = repository;
    }

    /**
     * To generate the Transaction, which contains all the items that were bought on the days that the user purchased this item.
     *
     * @param item the item from grocery list which needs to get connection suggestion item.
     * @return ArrayList<ArrayList < String>>, to store the transaction.
     */
    public List<Set<String>> generateTransaction(String item) {
        List<P_item> pastPurchases = pastPurchasesListRepository.findByName(item);
        List<Set<String>> transactions = new ArrayList<>();
        Set<LocalDate> processedDates = new HashSet<>();

        for (P_item purchase : pastPurchases) {
            LocalDate purchaseDate = purchase.getAddDate();

            if (!processedDates.contains(purchaseDate)) {
                List<P_item> itemsPurchasedOnSameDate = pastPurchasesListRepository.findByAddDate(purchaseDate);
                Set<String> transaction = new HashSet<>();
                for (P_item itemPurchasedOnSameDate : itemsPurchasedOnSameDate) {
                    transaction.add(itemPurchasedOnSameDate.getName());
                }
                transactions.add(transaction);
                processedDates.add(purchaseDate);
            }
        }
        return transactions;
    }


    private double calculateSupport(Set<String> itemSet, List<Set<String>> transactions) {
        int count = 0;
        for (Set<String> transaction : transactions) {
            if (transaction.containsAll(itemSet)) {
                count++;
            }
        }
        return count / (double
                ) transactions.size();
    }

    // pruning
    private void pruneCandidates(Map<Set<String>, Double> candidates) {
        candidates.entrySet().removeIf(entry -> entry.getValue() < minSupport);
    }

    // generate candidate
    private Map<Set<String>, Double> generateNextCandidates(Set<Set<String>> currentFrequentItemSets, List<Set<String>> transactions) {
        Map<Set<String>, Double> nextCandidates = new HashMap<>();
        List<Set<String>> currentFrequentItemList = new ArrayList<>(currentFrequentItemSets);

        for (int i = 0; i < currentFrequentItemList.size(); i++) {
            for (int j = i + 1; j < currentFrequentItemList.size(); j++) {
                Set<String> candidate = new HashSet<>(currentFrequentItemList.get(i));
                candidate.addAll(currentFrequentItemList.get(j));

                if (candidate.size() == currentFrequentItemList.get(i).size() + 1) {
                    double support = calculateSupport(candidate, transactions);
                    nextCandidates.put(candidate, support);
                }
            }
        }

        return nextCandidates;
    }


    public Set<Set<String>> generateFrequentItemSets(String item) {
        List<Set<String>> transactions = generateTransaction(item);
        Set<Set<String>> frequentItemSets = new HashSet<>();
        Map<Set<String>, Double> candidates = new HashMap<>();

        // first iteration
        for (Set<String> transaction : transactions) {
            for (String itemInTransaction : transaction) {
                Set<String> singleItemSet = Collections.singleton(itemInTransaction);
                candidates.compute(singleItemSet, (key, oldValue) -> oldValue == null ? 1 : oldValue + 1);
            }
        }

        // calculate support and pruning
        candidates.forEach((key, value) -> candidates.put(key, value / transactions.size()));
        pruneCandidates(candidates);
        frequentItemSets.addAll(candidates.keySet());

        // generate new candidates according to frequent set and calculate support.
        Set<Set<String>> currentFrequentItemSets = new HashSet<>(frequentItemSets);
        while (!currentFrequentItemSets.isEmpty()) {
            Map<Set<String>, Double> newCandidates = generateNextCandidates(currentFrequentItemSets, transactions);
            pruneCandidates(newCandidates);
            currentFrequentItemSets = newCandidates.keySet();
            frequentItemSets.addAll(currentFrequentItemSets);
        }

        return frequentItemSets;
    }

    /**
     * generate the most recommended items according to the input item name.
     *
     * @param item
     *         the name of the item that is added to grocery list.
     * @return Set<String>
     */

    public Set<String> generateRecommendations(String item) {
        Set<Set<String>> frequentItemSets = generateFrequentItemSets(item);
        Set<String> recommendations = new HashSet<>();


        for (Set<String> itemSet : frequentItemSets) {
            if (itemSet.size() > 1 && itemSet.contains(item)) {
                Set<String> recommendedItems = new HashSet<>(itemSet);
                recommendedItems.remove(item);
                recommendations.addAll(recommendedItems);
            }
        }

        return recommendations;
    }

}



