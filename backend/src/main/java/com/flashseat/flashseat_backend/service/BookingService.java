package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.dto.BookingCreateRequest;
import com.flashseat.flashseat_backend.dto.BookingResponse;
import com.flashseat.flashseat_backend.entity.*;
import com.flashseat.flashseat_backend.exception.*;
import com.flashseat.flashseat_backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public BookingService(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            SeatRepository seatRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public BookingResponse createBooking(
            Long eventId,
            Long userId,
            BookingCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event " + eventId + " not found"));

        List<Long> requestedSeatIds = request.seatIds();

        if (requestedSeatIds.size() != requestedSeatIds.stream().distinct().count()) {
            throw new IllegalArgumentException("Duplicate seat Ids are not allowed");
        }

        requestedSeatIds = requestedSeatIds.stream().sorted().toList();

        List<Seat> seats = seatRepository.findByIdInForUpdate(requestedSeatIds);

        if (seats.size() != requestedSeatIds.size()){
            throw new SeatNotFoundException("One or more seats not found");
        }

        for (Seat seat : seats){
            if (!seat.getEvent().getId().equals(eventId)) {
                throw new InvalidSeatException(
                        "Seat " + seat.getSeatNumber() + " does not belong to this event"
                );
            }

            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatNotAvailableException("Seat " + seat.getSeatNumber() + " is not available");
            }
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plusMinutes(5);

        Booking booking = Booking.builder()
                .user(user)
                .event(event)
                .status(BookingStatus.RESERVED)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        for (Seat seat : seats){
            seat.setStatus(SeatStatus.RESERVED);

            BookingSeatId bookingSeatId = new BookingSeatId(
                    savedBooking.getId(),
                    seat.getId()
            );

            BookingSeat bookingSeat = BookingSeat.builder()
                    .id(bookingSeatId)
                    .seat(seat)
                    .build();

            savedBooking.addBookingSeat(bookingSeat);
            bookingSeatRepository.save(bookingSeat);
        }

        return toBookingResponse(savedBooking);
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
