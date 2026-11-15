package com.gym.api.exercise;

public record ExerciseResponse(Long id, String name, int favorite) {

    static ExerciseResponse from(Exercise e) {
        return new ExerciseResponse(e.getId(), e.getName(), e.getFavorite());
    }
}
