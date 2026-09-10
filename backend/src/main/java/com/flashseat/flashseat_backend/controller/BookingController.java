package com.flashseat.flashseat_backend.controller;

import com.flashseat.flashseat_backend.dto.BookingCreateRequest;
import com.flashseat.flashseat_backend.dto.BookingResponse;
import com.flashseat.flashseat_backend.entity.User;
import com.flashseat.flashseat_backend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/events/{eventId}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @PathVariable Long eventId,
            @Valid @RequestBody BookingCreateRequest request
            ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();
        Long userId = authenticatedUser.getId();
        return bookingService.createBooking(
                eventId, userId, request);
    }

    @PostMapping("/bookings/{bookingId}/confirm")
    @PreAuthorize("hasRole('USER')")
    public BookingResponse confirmBooking(@PathVariable Long bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();

        Long userId = authenticatedUser.getId();
        return bookingService.confirmBooking(bookingId, userId);
    }

    @GetMapping("/bookings/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    public BookingResponse getBookingById(@PathVariable Long bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();

        Long userId = authenticatedUser.getId();
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping("/users/bookings")
    @PreAuthorize("hasRole('USER')")
    public List<BookingResponse> getMyBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();

        Long userId = authenticatedUser.getId();
        return bookingService.getBookingsByUserId(userId);
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public BookingResponse cancelBooking( @PathVariable Long bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();

        Long userId = authenticatedUser.getId();

        return bookingService.cancelBooking(bookingId, userId);
    }
}
