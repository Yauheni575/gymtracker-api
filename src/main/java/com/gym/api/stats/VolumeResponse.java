package com.gym.api.stats;

import java.time.LocalDate;
import java.util.List;

public record VolumeResponse(LocalDate from, LocalDate to, double total, long sets, List<DayVolume> days) {
}
