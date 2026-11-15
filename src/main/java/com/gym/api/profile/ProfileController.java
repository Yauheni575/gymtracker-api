package com.gym.api.profile;

import com.gym.api.auth.CurrentUserId;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ProfileDto get(@CurrentUserId Long userId) {
        return service.get(userId);
    }

    @PutMapping
    public ProfileDto save(@CurrentUserId Long userId, @Valid @RequestBody ProfileDto dto) {
        return service.save(userId, dto);
    }
}
