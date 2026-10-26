package com.smartkitchen.repository;

import com.smartkitchen.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ABSTRACTION: we only declare an interface. Spring Data JPA writes the
 * implementation (save, findById, findAll, delete, count ...) for us.
 */
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    /** Used for the "you may already have this item" warning. */
    boolean existsByNameIgnoreCase(String name);
}
