package com.gym.api.bodyweight;

import com.gym.api.auth.CurrentUserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/body-weight")
public class BodyWeightController {

    private final BodyWeightService service;

    public BodyWeightController(BodyWeightService service) {
        this.service = service;
    }

    @GetMapping
    public List<BodyWeightResponse> find(@CurrentUserId Long userId,
                                         @RequestParam(defaultValue = "1970-01-01") LocalDate from,
                                         @RequestParam(defaultValue = "9999-12-31") LocalDate to) {
        return service.find(userId, from, to);
    }

    @PutMapping("/{date}")
    public BodyWeightResponse save(@CurrentUserId Long userId, @PathVariable LocalDate date,
                                   @Valid @RequestBody BodyWeightRequest request) {
        return service.save(userId, date, request);
    }

    @DeleteMapping("/{date}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId Long userId, @PathVariable LocalDate date) {
        service.delete(userId, date);
    }
}
