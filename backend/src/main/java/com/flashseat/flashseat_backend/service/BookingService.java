package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.dto.BookingCreateRequest;
import com.flashseat.flashseat_backend.dto.BookingResponse;
import com.flashseat.flashseat_backend.entity.*;
import com.flashseat.flashseat_backend.exception.*;
import com.flashseat.flashseat_backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RedisLockService redisLockService;
    private final BookingTxService bookingTxService;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            RedisLockService redisLockService,
            BookingTxService bookingTxService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.redisLockService = redisLockService;
        this.bookingTxService = bookingTxService;
    }

    public BookingResponse createBooking(
            Long eventId,
            Long userId,
            BookingCreateRequest request
    ) {
        List<Long> requestedSeatIds = request.seatIds();

        if (requestedSeatIds.size() != requestedSeatIds.stream().distinct().count()) {
            throw new IllegalArgumentException("Duplicate seat Ids are not allowed");
        }

        List<Long> sortedSeatIds = requestedSeatIds.stream().sorted().toList();

        List<String> lockKeys = sortedSeatIds.stream()
                .map(seatId -> "lock:event:" + eventId + ":seat:" + seatId)
                .toList();

        // 1. Acquire Redis locks atomically for all requested seats
        Map<String, String> acquiredLocks = redisLockService.acquireLocks(lockKeys, Duration.ofSeconds(10));

        if (acquiredLocks == null) {
            throw new SeatNotAvailableException("One or more seats are currently being reserved by another user.");
        }

        try {
            // 2. Delegate DB operations to Spring proxy bean (@Transactional)
            return bookingTxService.executeTransactionalBooking(eventId, userId, sortedSeatIds);
        } finally {
            // 3. Release Redis locks
            for (Map.Entry<String, String> entry : acquiredLocks.entrySet()) {
                redisLockService.releaseLock(entry.getKey(), entry.getValue());
            }
        }
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking " + bookingId + " not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You are not allowed to confirm this booking"
            );
        }

        if (booking.getStatus() != BookingStatus.RESERVED){
            throw new BookingInvalidStateException(
                    "Booking " + bookingId + " cannot be confirmed because its status is "
                    + booking.getStatus()
            );
        }

        if (!booking.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new BookingInvalidStateException(
                    "Booking " + bookingId + " has expired"
            );
        }

        for (BookingSeat bookingSeat : booking.getBookingSeats()){
            Seat seat = bookingSeat.getSeat();

            if (seat.getStatus() != SeatStatus.RESERVED){
                throw new BookingInvalidStateException(
                        "Seat " + seat.getSeatNumber() + " is not reserved for this booking"
                );
            }
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        for (BookingSeat bookingSeat : booking.getBookingSeats()){
            bookingSeat.getSeat().setStatus(SeatStatus.SOLD);
        }

        return toBookingResponse(booking);
    }

    @Transactional
    public BookingResponse getBookingById(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking " + bookingId + " not found"
                ));
        if (!booking.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You are not allowed to view this booking"
            );
        }

        return toBookingResponse(booking);
    }

    @Transactional
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    "User " + userId + " not found"
            );
        }

        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return bookings.stream()
                .map(this::toBookingResponse)
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking " + bookingId + " not found"
                ));

        if (!booking.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You are not allowed to cancel this booking"
            );
        }

        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new BookingInvalidStateException(
                    "Booking " + bookingId + " cannot be cancelled because its status is "
                    + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);

        for (BookingSeat bookingSeat : booking.getBookingSeats()) {
            Seat seat = bookingSeat.getSeat();

            if (seat.getStatus() == SeatStatus.RESERVED) {
                seat.setStatus(SeatStatus.AVAILABLE);
            }
        }

        return toBookingResponse(booking);
    }

    private BookingResponse toBookingResponse(Booking booking) {

        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getEvent().getId(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getExpiresAt(),
                booking.getBookingSeats()
                        .stream()
                        .map(bookingSeat -> bookingSeat.getSeat().getId())
                        .toList()
        );
    }
}
