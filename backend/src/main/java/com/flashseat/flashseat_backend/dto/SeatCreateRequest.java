package com.flashseat.flashseat_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SeatCreateRequest(

        @NotBlank
        String seatNumber,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal price
) {
}
