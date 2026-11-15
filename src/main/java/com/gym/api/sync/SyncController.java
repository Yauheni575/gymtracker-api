package com.gym.api.sync;

import com.gym.api.auth.CurrentUserId;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService service;

    public SyncController(SyncService service) {
        this.service = service;
    }

    @PostMapping
    public SyncData sync(@CurrentUserId Long userId, @Valid @RequestBody SyncData data) {
        return service.sync(userId, data);
    }
}
