package com.gym.api.stats;

import com.gym.api.auth.CurrentUserId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @GetMapping("/records")
    public List<RecordResponse> records(@CurrentUserId Long userId) {
        return service.records(userId);
    }

    @GetMapping("/volume")
    public VolumeResponse volume(@CurrentUserId Long userId,
                                 @RequestParam(required = false) LocalDate from,
                                 @RequestParam(required = false) LocalDate to) {
        return service.volume(userId, from, to);
    }
}
