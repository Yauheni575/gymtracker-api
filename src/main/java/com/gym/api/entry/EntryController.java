package com.gym.api.entry;

import com.gym.api.auth.CurrentUserId;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class EntryController {

    private final EntryService service;

    public EntryController(EntryService service) {
        this.service = service;
    }

    @GetMapping("/exercises/{exerciseId}/entries")
    public PagedModel<EntryResponse> find(@CurrentUserId Long userId,
                                          @PathVariable Long exerciseId,
                                          @RequestParam(defaultValue = "1970-01-01") LocalDate from,
                                          @RequestParam(defaultValue = "9999-12-31") LocalDate to,
                                          @PageableDefault(size = 50, sort = {"date", "id"}) Pageable pageable) {
        return service.find(userId, exerciseId, from, to, pageable);
    }

    @PostMapping("/exercises/{exerciseId}/entries")
    @ResponseStatus(HttpStatus.CREATED)
    public EntryResponse create(@CurrentUserId Long userId, @PathVariable Long exerciseId,
                                @Valid @RequestBody EntryRequest request) {
        return service.create(userId, exerciseId, request);
    }

    @DeleteMapping("/entries/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId Long userId, @PathVariable Long id) {
        service.delete(userId, id);
    }
}
