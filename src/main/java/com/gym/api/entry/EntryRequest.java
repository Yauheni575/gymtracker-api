package com.gym.api.entry;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record EntryRequest(
        @NotNull LocalDate date,
        @PositiveOrZero @Max(1000) double weight,
        @Positive @Max(1000) int reps) {
}
