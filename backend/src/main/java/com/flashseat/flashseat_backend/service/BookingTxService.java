package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.dto.BookingResponse;
import com.flashseat.flashseat_backend.entity.*;
import com.flashseat.flashseat_backend.exception.*;
import com.flashseat.flashseat_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BookingTxService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public BookingTxService(
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
    public BookingResponse executeTransactionalBooking(
            Long eventId,
            Long userId,
            List<Long> sortedSeatIds
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event " + eventId + " not found"));

        List<Seat> seats = seatRepository.findByIdInForUpdate(sortedSeatIds);

        if (seats.size() != sortedSeatIds.size()) {
            throw new SeatNotFoundException("One or more seats not found");
        }

        for (Seat seat : seats) {
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

        for (Seat seat : seats) {
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