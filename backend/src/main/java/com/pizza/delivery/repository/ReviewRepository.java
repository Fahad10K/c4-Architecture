package com.pizza.delivery.repository;

import com.pizza.delivery.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByMenuItemId(String menuItemId);
    List<Review> findByUserId(String userId);
}
