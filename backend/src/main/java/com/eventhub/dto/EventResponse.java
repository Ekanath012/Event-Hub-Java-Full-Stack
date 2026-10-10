package com.eventhub.dto;

import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventResponse(
        Long id, String title, String description, String bannerImage,
        CategoryResponse category, VenueResponse venue,
        Long organizerId, String organizerName,
        LocalDate eventDate, LocalTime startTime, LocalTime endTime,
        LocalDate registrationStart, LocalDate registrationDeadline,
        Integer maxAttendees, EventStatus status, Integer availableSeats,
        String rules, String contactEmail, String contactPhone,
        Instant createdAt, Instant updatedAt
) {
    public static EventResponse from(Event e, int registeredCount) {
        int available = Math.max(0, e.getMaxAttendees() - registeredCount);
        return new EventResponse(
                e.getId(), e.getTitle(), e.getDescription(), e.getBannerImage(),
                CategoryResponse.from(e.getCategory()), VenueResponse.from(e.getVenue()),
                e.getOrganizer().getId(), e.getOrganizer().getFirstName() + " " + e.getOrganizer().getLastName(),
                e.getEventDate(), e.getStartTime(), e.getEndTime(),
                e.getRegistrationStart(), e.getRegistrationDeadline(), e.getMaxAttendees(), e.getStatus(), available,
                e.getRules(), e.getContactEmail(), e.getContactPhone(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
