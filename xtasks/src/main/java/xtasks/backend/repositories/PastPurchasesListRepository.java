package xtasks.backend.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xtasks.backend.entities.P_item;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PastPurchasesListRepository extends CrudRepository<P_item,Long> {
    List<P_item> findAll();

    List<P_item> findByName(String name);

    List<P_item> findByAddDate(LocalDate addDate);

    Optional<P_item> findByAddDateAndNameAndUnitAndCategory(
            LocalDate addDate, String name, String unit, String category);
}
