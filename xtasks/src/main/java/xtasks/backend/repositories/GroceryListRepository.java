package xtasks.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import xtasks.backend.entities.L_item;
import xtasks.backend.entities.G_item;
import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface GroceryListRepository extends JpaRepository<L_item,Long> {
    List<L_item> findAll();
    List<L_item> findByCatalogueItem(G_item gItem);

    void deleteByListItemIdIn(Set<Long> listItemId);

    boolean existsByCatalogueItem(G_item gItem);
}
