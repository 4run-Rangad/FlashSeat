package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.dto.SeatCreateRequest;
import com.flashseat.flashseat_backend.dto.SeatResponse;
import com.flashseat.flashseat_backend.entity.Event;
import com.flashseat.flashseat_backend.entity.Seat;
import com.flashseat.flashseat_backend.exception.EventNotFoundException;
import com.flashseat.flashseat_backend.exception.SeatAlreadyExistsException;
import com.flashseat.flashseat_backend.repository.EventRepository;
import com.flashseat.flashseat_backend.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;

    public SeatService(
            SeatRepository seatRepository,
            EventRepository eventRepository
    ) {
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
    }

    public SeatResponse createSeat(Long eventId, SeatCreateRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event " + eventId + " not found"));

        if(seatRepository.existsByEventIdAndSeatNumber(
                eventId,
                request.seatNumber()
        )){
            throw new SeatAlreadyExistsException(
                    "Seat " + request.seatNumber() + " already exists for this event"
            );
        }

        Seat seat = new Seat();
        seat.setEvent(event);
        seat.setSeatNumber(request.seatNumber());
        seat.setPrice(request.price());

        Seat savedSeat = seatRepository.save(seat);

        return toResponse(savedSeat);
    }

    public List<SeatResponse> getSeatByEvent(Long eventId){

        if (!eventRepository.existsById(eventId)){
            throw new EventNotFoundException("Event " + eventId + " not found");
        }

        return seatRepository.findByEventId(eventId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SeatResponse toResponse(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getEvent().getId(),
                seat.getSeatNumber(),
                seat.getPrice(),
                seat.getStatus()
        );
    }
}
