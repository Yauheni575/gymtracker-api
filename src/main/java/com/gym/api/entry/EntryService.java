package com.gym.api.entry;

import com.gym.api.exercise.ExerciseService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class EntryService {

    private final EntryRepository repository;
    private final ExerciseService exercises;

    public EntryService(EntryRepository repository, ExerciseService exercises) {
        this.repository = repository;
        this.exercises = exercises;
    }

    public List<EntryResponse> find(Long userId, Long exerciseId, LocalDate from, LocalDate to) {
        exercises.get(userId, exerciseId);
        return repository.findByExerciseIdAndDateBetweenOrderByDateAscIdAsc(exerciseId, from, to)
                .stream().map(EntryResponse::from).toList();
    }

    @Transactional
    public EntryResponse create(Long userId, Long exerciseId, EntryRequest request) {
        Entry entry = new Entry(exercises.get(userId, exerciseId), request.date(), request.weight(), request.reps());
        return EntryResponse.from(repository.save(entry));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Entry entry = repository.findByIdAndExercise_UserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Подход не найден"));
        repository.delete(entry);
    }
}
