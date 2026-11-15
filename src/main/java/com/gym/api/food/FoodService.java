package com.gym.api.food;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class FoodService {

    private final FoodRepository repository;

    public FoodService(FoodRepository repository) {
        this.repository = repository;
    }

    public List<FoodResponse> find(Long userId, LocalDate date) {
        return repository.findByUserIdAndDateOrderByIdAsc(userId, date).stream().map(Food::toResponse).toList();
    }

    @Transactional
    public FoodResponse create(Long userId, FoodRequest request) {
        return repository.save(new Food(userId, request)).toResponse();
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Food food = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена"));
        repository.delete(food);
    }
}
