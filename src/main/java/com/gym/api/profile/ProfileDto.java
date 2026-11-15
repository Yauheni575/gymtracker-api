package com.gym.api.profile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProfileDto(
        @Size(max = 100) String name,
        @NotBlank @Size(max = 20) String sex,
        @Min(0) @Max(120) int age,
        @Min(0) @Max(250) int height,
        @PositiveOrZero @Max(400) double target,
        @NotBlank @Size(max = 30) String goal,
        @NotBlank @Size(max = 30) String experience,
        @Min(1) @Max(7) int perWeek,
        @Size(max = 300) String priorities) {
}
