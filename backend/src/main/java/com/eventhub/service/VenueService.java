package com.eventhub.service;

import com.eventhub.dto.VenueRequest;
import com.eventhub.dto.VenueResponse;
import com.eventhub.entity.Venue;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    public List<VenueResponse> list() {
        return venueRepository.findAllByOrderByCityAscNameAsc().stream().map(VenueResponse::from).toList();
    }

    @Transactional
    public VenueResponse create(VenueRequest request) {
        Venue v = Venue.builder().name(request.name().trim()).address(request.address().trim()).city(request.city().trim())
                .state(request.state()).postalCode(request.postalCode()).active(request.active() == null || request.active()).build();
        return VenueResponse.from(venueRepository.save(v));
    }

    @Transactional
    public VenueResponse update(Long id, VenueRequest request) {
        Venue v = get(id);
        v.setName(request.name().trim());
        v.setAddress(request.address().trim());
        v.setCity(request.city().trim());
        v.setState(request.state());
        v.setPostalCode(request.postalCode());
        if (request.active() != null) v.setActive(request.active());
        return VenueResponse.from(venueRepository.save(v));
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (eventRepository.existsByVenueId(id)) throw new IllegalArgumentException("Venue is used by an event and cannot be deleted");
        venueRepository.deleteById(id);
    }

    public Venue get(Long id) {
        return venueRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Venue not found"));
    }
}
