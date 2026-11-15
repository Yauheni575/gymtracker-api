package com.gym.api.bodyweight;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class BodyWeightService {

    private final BodyWeightRepository repository;

    public BodyWeightService(BodyWeightRepository repository) {
        this.repository = repository;
    }

    public List<BodyWeightResponse> find(Long userId, LocalDate from, LocalDate to) {
        return repository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to)
                .stream().map(BodyWeightResponse::from).toList();
    }

    @Transactional
    public BodyWeightResponse save(Long userId, LocalDate date, BodyWeightRequest request) {
        BodyWeight bodyWeight = repository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> new BodyWeight(userId, date));
        bodyWeight.setWeight(request.weight());
        return BodyWeightResponse.from(repository.save(bodyWeight));
    }

    @Transactional
    public void delete(Long userId, LocalDate date) {
        BodyWeight bodyWeight = repository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Нет записи веса за эту дату"));
        repository.delete(bodyWeight);
    }
}
