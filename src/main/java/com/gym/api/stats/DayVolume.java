package com.gym.api.stats;

import java.time.LocalDate;

public record DayVolume(LocalDate date, Double volume, Long sets) {
}
