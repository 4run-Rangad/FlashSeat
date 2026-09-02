package com.flashseat.flashseat_backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record EventCreateRequest(

        @NotBlank
        String name,

        @NotBlank
        String venue,

        @NotNull
        @Future
        OffsetDateTime startTime,

        @NotNull
        @Future
        OffsetDateTime endTime
) {
}
