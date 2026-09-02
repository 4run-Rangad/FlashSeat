package com.flashseat.flashseat_backend.dto;

import java.time.OffsetDateTime;

public record UserResponse(

        Long id,
        String email,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
