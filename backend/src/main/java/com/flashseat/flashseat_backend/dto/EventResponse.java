package com.flashseat.flashseat_backend.dto;

import java.time.OffsetDateTime;

public record EventResponse(
        Long id,
        String name,
        String venue,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {
}
