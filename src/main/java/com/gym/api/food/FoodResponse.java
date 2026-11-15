package com.gym.api.food;

import java.time.LocalDate;

public record FoodResponse(Long id, LocalDate date, String name, double grams,
                           double kcal, double protein, double fat, double carbs, String code) {
}
