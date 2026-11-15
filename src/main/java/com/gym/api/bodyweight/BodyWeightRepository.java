package com.gym.api.bodyweight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BodyWeightRepository extends JpaRepository<BodyWeight, Long> {

    List<BodyWeight> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);

    Optional<BodyWeight> findByUserIdAndDate(Long userId, LocalDate date);
}
