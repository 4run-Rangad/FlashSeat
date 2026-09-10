package com.flashseat.flashseat_backend.controller;

import com.flashseat.flashseat_backend.dto.SeatCreateRequest;
import com.flashseat.flashseat_backend.dto.SeatResponse;
import com.flashseat.flashseat_backend.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService){
        this.seatService = seatService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SeatResponse createSeat(
            @PathVariable Long eventId,
            @Valid @RequestBody SeatCreateRequest request
    ){
        return seatService.createSeat(eventId, request);
    }

    @GetMapping
    public List<SeatResponse> getSeatsByEvent(@PathVariable Long eventId) {
        return seatService.getSeatByEvent(eventId);

    }
}
