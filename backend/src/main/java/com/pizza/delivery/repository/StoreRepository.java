package com.pizza.delivery.repository;

import com.pizza.delivery.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    List<Store> findByIsActiveTrue();
    List<Store> findByCityIgnoreCase(String city);
    List<Store> findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(String name, String city);

    @Query("SELECT s FROM Store s WHERE s.isActive = true AND " +
           "SQRT(POWER(s.lat - :lat, 2) + POWER(s.lng - :lng, 2)) * 111 <= :radiusKm")
    List<Store> findNearbyStores(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusKm") double radiusKm);
}
