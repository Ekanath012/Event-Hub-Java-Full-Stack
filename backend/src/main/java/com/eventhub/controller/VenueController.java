package com.eventhub.controller;

import com.eventhub.dto.VenueRequest;
import com.eventhub.dto.VenueResponse;
import com.eventhub.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {
    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<List<VenueResponse>> list() { return ResponseEntity.ok(venueService.list()); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<VenueResponse> create(@Valid @RequestBody VenueRequest request) { return ResponseEntity.ok(venueService.create(request)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<VenueResponse> update(@PathVariable Long id, @Valid @RequestBody VenueRequest request) { return ResponseEntity.ok(venueService.update(id, request)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { venueService.delete(id); return ResponseEntity.noContent().build(); }
}
