package com.gym.api.exercise;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExerciseRequest(
        @NotBlank @Size(max = 100) String name,
        @Min(0) @Max(127) int favorite) {
}
