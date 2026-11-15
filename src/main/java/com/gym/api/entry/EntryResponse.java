package com.gym.api.entry;

import java.time.LocalDate;

public record EntryResponse(Long id, Long exerciseId, LocalDate date, double weight, int reps) {

    static EntryResponse from(Entry e) {
        return new EntryResponse(e.getId(), e.getExercise().getId(), e.getDate(), e.getWeight(), e.getReps());
    }
}
