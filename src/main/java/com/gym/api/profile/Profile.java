package com.gym.api.profile;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Profile {

    @Id
    private Long userId;

    private String name;
    private String sex;
    private int age;
    private int height;
    private double target;
    private String goal;
    private String experience;
    private int perWeek;
    private String priorities;

    protected Profile() {
    }

    public Profile(Long userId) {
        this.userId = userId;
    }

    void apply(ProfileDto dto) {
        name = dto.name() == null ? "" : dto.name().trim();
        sex = dto.sex();
        age = dto.age();
        height = dto.height();
        target = dto.target();
        goal = dto.goal();
        experience = dto.experience();
        perWeek = dto.perWeek();
        priorities = dto.priorities() == null ? "" : dto.priorities();
    }

    ProfileDto toDto() {
        return new ProfileDto(name, sex, age, height, target, goal, experience, perWeek, priorities);
    }
}
