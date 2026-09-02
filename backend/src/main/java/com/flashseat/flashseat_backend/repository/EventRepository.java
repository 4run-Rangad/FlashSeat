package com.flashseat.flashseat_backend.repository;

import com.flashseat.flashseat_backend.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

}