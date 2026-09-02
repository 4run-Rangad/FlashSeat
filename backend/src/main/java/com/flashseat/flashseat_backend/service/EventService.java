package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.dto.EventCreateRequest;
import com.flashseat.flashseat_backend.dto.EventResponse;
import com.flashseat.flashseat_backend.entity.Event;
import com.flashseat.flashseat_backend.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    private EventResponse toResponse(Event event){
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getVenue(),
                event.getStartTime(),
                event.getEndTime()
        );
    }

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
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
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return toResponse(event);
    }
}
