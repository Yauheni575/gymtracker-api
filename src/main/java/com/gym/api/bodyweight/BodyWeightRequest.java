package com.gym.api.bodyweight;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record BodyWeightRequest(@Positive @Max(400) double weight) {
}
