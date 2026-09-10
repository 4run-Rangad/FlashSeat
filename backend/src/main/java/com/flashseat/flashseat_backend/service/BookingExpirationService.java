package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.entity.Booking;
import com.flashseat.flashseat_backend.entity.BookingStatus;
import com.flashseat.flashseat_backend.entity.Seat;
import com.flashseat.flashseat_backend.entity.SeatStatus;
import com.flashseat.flashseat_backend.repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class BookingExpirationService {

    private final BookingRepository bookingRepository;

    public BookingExpirationService(BookingRepository bookingRepository){
        this.bookingRepository = bookingRepository;
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void expireBookings(){

        OffsetDateTime now = OffsetDateTime.now();

        var expiredBooking = bookingRepository.findExpiredBookingsForUpdate(
                BookingStatus.RESERVED,
                now
        );

        for(Booking booking : expiredBooking) {
            booking.setStatus(BookingStatus.EXPIRED);

            for (var bookingSeat : booking.getBookingSeats()) {
                Seat seat = bookingSeat.getSeat();
                seat.setStatus(SeatStatus.AVAILABLE);
            }
        }
    }
}
