package com.pizza.delivery.repository;

import com.pizza.delivery.entity.User;
import com.pizza.delivery.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRole role);
    Page<User> findByRole(UserRole role, Pageable pageable);
    List<User> findByIsActiveTrue();
    long countByIsActiveTrue();
    long countByRole(UserRole role);
}
