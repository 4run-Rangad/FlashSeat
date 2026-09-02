package com.flashseat.flashseat_backend.repository;

import com.flashseat.flashseat_backend.entity.Booking;
import com.flashseat.flashseat_backend.entity.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatusAndExpiresAtBefore(
            BookingStatus status,
            OffsetDateTime time
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.status = :status
            AND b.expiresAt < :time
            """)
    List<Booking> findExpiredBookingsForUpdate(
            @Param("status") BookingStatus status,
            @Param("time") OffsetDateTime time
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.id = :bookingId
            """)
    Optional<Booking> findByIdForUpdate(
            @Param("bookingId") Long bookingId
    );

    List<Booking> findByUserIdOrderByCreatedAtDesc(long userId);
}