package com.pizza.delivery.repository;

import com.pizza.delivery.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findByUserId(String userId);
    void deleteByIdAndUserId(String id, String userId);
}
