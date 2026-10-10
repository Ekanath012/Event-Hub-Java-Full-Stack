package com.eventhub.dto;

import com.eventhub.entity.Venue;

public record VenueResponse(Long id, String name, String address, String city, String state, String postalCode, boolean active) {
    public static VenueResponse from(Venue v) {
        return new VenueResponse(v.getId(), v.getName(), v.getAddress(), v.getCity(), v.getState(), v.getPostalCode(), v.isActive());
    }
}
