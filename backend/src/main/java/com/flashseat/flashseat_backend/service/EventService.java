package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.dto.EventCreateRequest;
import com.flashseat.flashseat_backend.dto.EventResponse;
import com.flashseat.flashseat_backend.dto.EventUpdateRequest;
import com.flashseat.flashseat_backend.entity.Booking;
import com.flashseat.flashseat_backend.entity.Event;
import com.flashseat.flashseat_backend.entity.Seat;
import com.flashseat.flashseat_backend.exception.EventNotFoundException;
import com.flashseat.flashseat_backend.repository.BookingRepository;
import com.flashseat.flashseat_backend.repository.EventRepository;
import com.flashseat.flashseat_backend.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;

    private EventResponse toResponse(Event event){
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getVenue(),
                event.getStartTime(),
                event.getEndTime()
        );
    }

    public EventService(
            EventRepository eventRepository,
            SeatRepository seatRepository,
            BookingRepository bookingRepository
    ) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
    }

    public EventResponse createEvent(EventCreateRequest request){

        Event event = Event.builder()
                .name(request.name())
                .venue(request.venue())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        Event savedEvent = eventRepository.save(event);

        return toResponse(savedEvent);
    }

    public List<EventResponse> getAllEvents(){
        return eventRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponse getEventById(Long id){
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event " + id + " not found"));
        return toResponse(event);
    }

    public EventResponse updateEvent(Long id, EventUpdateRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(
                        "Event " + id + " not found"
                ));

        if (!request.startTime().isBefore(request.endTime()))  {
            throw new IllegalArgumentException(
                    "Start time must be before end time"
            );
        }

        event.setName(request.name());
        event.setVenue(request.venue());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());

        Event updatedEvent = eventRepository.save(event);

        return toResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(
                        "Event " + id + " not found"
                ));

        List<Booking> bookings = bookingRepository.findByEventId(id);

        if (!bookings.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete an event that has bookings"
            );
        }

        List<Seat> seats = seatRepository.findByEventId(id);

        seatRepository.deleteAll(seats);
        eventRepository.delete(event);
    }
}
