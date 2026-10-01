package com.flashseat.flashseat_backend.repository;

import com.flashseat.flashseat_backend.entity.BookingSeat;
import com.flashseat.flashseat_backend.entity.BookingSeatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, BookingSeatId> {
    @Modifying
    @Transactional
    @Query("DELETE FROM BookingSeat bs WHERE bs.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}