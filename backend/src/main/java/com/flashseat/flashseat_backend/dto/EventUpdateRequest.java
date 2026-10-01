package com.flashseat.flashseat_backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record EventUpdateRequest(

        @NotBlank(message = "Event name is required")
        String name,

        @NotBlank(message = "Venue is required")
        String venue,

        @NotNull(message = "Start time is required")
        @Future
        OffsetDateTime startTime,

        @NotNull(message = "End time is required")
        @Future
        OffsetDateTime endTime
) {
}
