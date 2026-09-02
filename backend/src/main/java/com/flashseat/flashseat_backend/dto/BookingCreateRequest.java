package com.flashseat.flashseat_backend.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BookingCreateRequest(
        @NotEmpty
        List<Long> seatIds
) {
}
