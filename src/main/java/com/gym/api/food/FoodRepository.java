package com.gym.api.food;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {

    List<Food> findByUserIdAndDateOrderByIdAsc(Long userId, LocalDate date);

    Optional<Food> findByIdAndUserId(Long id, Long userId);
}
