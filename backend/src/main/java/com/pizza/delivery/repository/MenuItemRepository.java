package com.pizza.delivery.repository;

import com.pizza.delivery.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {
    List<MenuItem> findByStoreIdAndIsAvailableTrue(String storeId);
    List<MenuItem> findByStoreIdAndCategoryId(String storeId, String categoryId);
    List<MenuItem> findByIsPopularTrueAndIsAvailableTrue();
    List<MenuItem> findByIsAvailableTrue();
    List<MenuItem> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<MenuItem> searchItems(@Param("query") String query);
}
