package com.gym.api.stats;

import java.time.LocalDate;

public record RecordResponse(Long exerciseId, String name, Double maxWeight, Long sets, LocalDate lastTrained) {
}
