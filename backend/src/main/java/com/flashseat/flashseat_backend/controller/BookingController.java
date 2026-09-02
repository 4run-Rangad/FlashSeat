package com.flashseat.flashseat_backend.controller;

import com.flashseat.flashseat_backend.dto.BookingCreateRequest;
import com.flashseat.flashseat_backend.dto.BookingResponse;
import com.flashseat.flashseat_backend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/events/{eventId}/bookings/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @Valid @RequestBody BookingCreateRequest request
            ) {
        return bookingService.createBooking(
                eventId, userId, request);
    }

    @PostMapping("/bookings/{bookingId}/confirm")
    public BookingResponse confirmBooking(@PathVariable Long bookingId) {
        return bookingService.confirmBooking(bookingId);
    }

    @GetMapping("/bookings/{bookingId}")
    public BookingResponse getBookingById(@PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    @GetMapping("/users/{userId}/bookings")
    public List<BookingResponse> getBookingsByUserId(
            @PathVariable Long userId
    ) {
        return bookingService.getBookingsByUserId(userId);
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public BookingResponse cancelBooking( @PathVariable Long bookingId) {
        return bookingService.cancelBooking(bookingId);
    }
}
