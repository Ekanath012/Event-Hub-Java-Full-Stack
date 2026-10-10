package com.eventhub.controller;

import com.eventhub.dto.EventRequest;
import com.eventhub.dto.EventResponse;
import com.eventhub.dto.EventStatusRequest;
import com.eventhub.entity.EventStatus;
import com.eventhub.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Authentication authentication) {
        return ResponseEntity.ok(eventService.search(q, categoryId, venueId, status, fromDate, toDate, page, size, sortBy, direction,
                authentication == null ? null : authentication.getName()));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Page<EventResponse>> mine(
            org.springframework.security.core.Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.mine(authentication.getName(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(eventService.getForViewer(id, authentication == null ? null : authentication.getName()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<EventResponse> create(org.springframework.security.core.Authentication authentication,
                                                 @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<EventResponse> update(org.springframework.security.core.Authentication authentication,
                                                 @PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.update(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> delete(org.springframework.security.core.Authentication authentication, @PathVariable Long id) {
        eventService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<EventResponse> changeStatus(org.springframework.security.core.Authentication authentication,
                                                       @PathVariable Long id, @Valid @RequestBody EventStatusRequest request) {
        return ResponseEntity.ok(eventService.changeStatus(authentication.getName(), id, request.status()));
    }
}
