package com.gym.api.sync;

import com.gym.api.profile.ProfileDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record SyncData(
        @PositiveOrZero Long cursor,
        @Size(max = 20000) List<@Valid ExerciseChange> exercises,
        @Size(max = 20000) List<@Valid EntryChange> entries,
        @Size(max = 20000) List<@Valid BodyWeightChange> bodyWeights,
        @Size(max = 20000) List<@Valid FoodChange> foods,
        @Valid ProfileDto profile) {

    public record ExerciseChange(
            @NotBlank @Size(max = 36) String uuid,
            @Size(max = 100) String name,
            @Min(0) @Max(127) Integer favorite,
            Boolean deleted) {
    }

    public record EntryChange(
            @NotBlank @Size(max = 36) String uuid,
            @Size(max = 36) String exerciseUuid,
            LocalDate date,
            @PositiveOrZero @Max(1000) Double weight,
            @PositiveOrZero @Max(1000) Integer reps,
            Boolean deleted) {
    }

    public record BodyWeightChange(
            @NotNull LocalDate date,
            @PositiveOrZero @Max(400) Double weight,
            Boolean deleted) {
    }

    public record FoodChange(
            @NotBlank @Size(max = 36) String uuid,
            LocalDate date,
            @Size(max = 200) String name,
            @PositiveOrZero @Max(10000) Double grams,
            @PositiveOrZero @Max(20000) Double kcal,
            @PositiveOrZero @Max(5000) Double protein,
            @PositiveOrZero @Max(5000) Double fat,
            @PositiveOrZero @Max(5000) Double carbs,
            @Size(max = 50) String code,
            Boolean deleted) {
    }
}
