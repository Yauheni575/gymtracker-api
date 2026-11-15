package com.gym.api.exercise;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository repository;

    public ExerciseService(ExerciseRepository repository) {
        this.repository = repository;
    }

    public List<ExerciseResponse> findAll(Long userId) {
        return repository.findByUserIdOrderByNameAsc(userId).stream().map(ExerciseResponse::from).toList();
    }

    public Exercise get(Long userId, Long id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Упражнение не найдено"));
    }

    @Transactional
    public ExerciseResponse create(Long userId, ExerciseRequest request) {
        String name = request.name().trim();
        checkNameFree(userId, name);
        return ExerciseResponse.from(repository.save(new Exercise(userId, name, request.favorite())));
    }

    @Transactional
    public ExerciseResponse update(Long userId, Long id, ExerciseRequest request) {
        Exercise exercise = get(userId, id);
        String name = request.name().trim();
        if (!name.equals(exercise.getName())) checkNameFree(userId, name);
        exercise.setName(name);
        exercise.setFavorite(request.favorite());
        return ExerciseResponse.from(exercise);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        repository.delete(get(userId, id));
    }

    private void checkNameFree(Long userId, String name) {
        if (repository.existsByUserIdAndName(userId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Упражнение уже есть");
        }
    }
}
