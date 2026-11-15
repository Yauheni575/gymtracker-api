package com.gym.api.bodyweight;

import java.time.LocalDate;

public record BodyWeightResponse(LocalDate date, double weight) {

    static BodyWeightResponse from(BodyWeight b) {
        return new BodyWeightResponse(b.getDate(), b.getWeight());
    }
}
