package com.gym.api.profile;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {

    private final ProfileRepository repository;

    public ProfileService(ProfileRepository repository) {
        this.repository = repository;
    }

    public ProfileDto get(Long userId) {
        return repository.findById(userId)
                .map(Profile::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Профиль ещё не заполнен"));
    }

    @Transactional
    public ProfileDto save(Long userId, ProfileDto dto) {
        Profile profile = repository.findById(userId).orElseGet(() -> new Profile(userId));
        profile.apply(dto);
        return repository.save(profile).toDto();
    }
}
