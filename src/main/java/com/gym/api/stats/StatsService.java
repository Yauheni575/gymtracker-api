package com.gym.api.stats;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class StatsService {

    private final StatsRepository repository;

    public StatsService(StatsRepository repository) {
        this.repository = repository;
    }

    public List<RecordResponse> records(Long userId) {
        return repository.records(userId);
    }

    public VolumeResponse volume(Long userId, LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(6);
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from не может быть позже to");
        }
        if (ChronoUnit.DAYS.between(start, end) > 366) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Период не больше года");
        }
        List<DayVolume> days = repository.volumeByDay(userId, start, end);
        double total = days.stream().mapToDouble(DayVolume::volume).sum();
        long sets = days.stream().mapToLong(DayVolume::sets).sum();
        return new VolumeResponse(start, end, total, sets, days);
    }
}
