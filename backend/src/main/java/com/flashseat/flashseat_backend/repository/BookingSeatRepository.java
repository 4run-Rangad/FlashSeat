package com.flashseat.flashseat_backend.repository;

import com.flashseat.flashseat_backend.entity.BookingSeat;
import com.flashseat.flashseat_backend.entity.BookingSeatId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, BookingSeatId> {

}