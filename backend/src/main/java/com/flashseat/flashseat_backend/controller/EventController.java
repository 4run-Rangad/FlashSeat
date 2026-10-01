package com.flashseat.flashseat_backend.controller;

import com.flashseat.flashseat_backend.dto.EventCreateRequest;
import com.flashseat.flashseat_backend.dto.EventResponse;
import com.flashseat.flashseat_backend.dto.EventUpdateRequest;
import com.flashseat.flashseat_backend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> getAllEvents(){
        return eventService.getAllEvents();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse createEvent(
            @Valid
            @RequestBody EventCreateRequest request
            ){
        return eventService.createEvent(request);
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request
            ) {
        return eventService.updateEvent(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }
}
