package com.gym.api.entry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    List<Entry> findByExerciseIdAndDateBetweenOrderByDateAscIdAsc(Long exerciseId, LocalDate from, LocalDate to);

    Optional<Entry> findByIdAndExercise_UserId(Long id, Long userId);
}
