package com.flashseat.flashseat_backend.dto;

import com.flashseat.flashseat_backend.entity.SeatStatus;

import java.math.BigDecimal;

public record SeatResponse(
        Long id,
        Long eventId,
        String seatNumber,
        BigDecimal price,
        SeatStatus status
) {
}
