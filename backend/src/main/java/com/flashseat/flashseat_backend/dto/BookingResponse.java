package com.flashseat.flashseat_backend.dto;

import com.flashseat.flashseat_backend.entity.BookingStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        Long userId,
        Long eventId,
        BookingStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        List<Long> seatIds
) {
}
