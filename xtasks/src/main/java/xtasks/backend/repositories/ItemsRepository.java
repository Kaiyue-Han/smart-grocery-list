package xtasks.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xtasks.backend.entities.G_item;

import java.util.Collection;
import java.util.List;
@Repository
public interface ItemsRepository extends JpaRepository<G_item,Long> {
    List<G_item> findAll();
    List<G_item> findByName(String name);
    List<G_item> findByNameIn(Collection<String> names);

    G_item findById(String name);

}
