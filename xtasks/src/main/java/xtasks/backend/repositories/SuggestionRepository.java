package xtasks.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import xtasks.backend.entities.S_item;
import xtasks.backend.entities.G_item;
import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface SuggestionRepository extends JpaRepository<S_item,Long> {
    List<S_item> findAll();
    List<S_item> findByCatalogueItem(G_item gItem);
    boolean existsByCatalogueItemNameAndReasonAndCatalogueItemUnit(String name, String reason, String unit);
    void deleteBySuggestionIdIn(Set<Long> suggestionId);
    void deleteAllByIsReject(boolean isReject);
}