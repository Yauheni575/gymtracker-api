package com.gym.api.entry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    Page<Entry> findByExerciseIdAndDateBetween(Long exerciseId, LocalDate from, LocalDate to, Pageable pageable);

    Optional<Entry> findByIdAndExercise_UserId(Long id, Long userId);
}
