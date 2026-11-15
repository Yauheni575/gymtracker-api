package com.gym.api.food;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FoodRequest(
        @NotNull LocalDate date,
        @NotBlank @Size(max = 200) String name,
        @Positive @Max(10000) double grams,
        @PositiveOrZero @Max(20000) double kcal,
        @PositiveOrZero @Max(5000) double protein,
        @PositiveOrZero @Max(5000) double fat,
        @PositiveOrZero @Max(5000) double carbs,
        @Size(max = 50) String code) {
}
