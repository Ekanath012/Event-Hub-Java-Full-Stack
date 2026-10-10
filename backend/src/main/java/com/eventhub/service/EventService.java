package com.eventhub.service;

import com.eventhub.dto.EventRequest;
import com.eventhub.dto.EventResponse;
import com.eventhub.entity.*;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final VenueService venueService;
    private final RegistrationRepository registrationRepository;
    private final AuditLogService audit;
    private final NotificationService notifications;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public Page<EventResponse> search(String q, Long categoryId, Long venueId, EventStatus status,
                                      LocalDate fromDate, LocalDate toDate, int page, int size, String sortBy, String direction,
                                      String viewerEmail) {
        if (page < 0) throw new IllegalArgumentException("Page cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) throw new IllegalArgumentException("fromDate cannot be after toDate");
        Sort sort = buildSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        // Drafts are visible to admins (all drafts) and to organizers (their own drafts) so that a
        // newly created event shows up in the list straight away.
        User viewer = viewerEmail == null ? null : userRepository.findByEmailIgnoreCase(viewerEmail).orElse(null);
        boolean admin = viewer != null && (viewer.getRole() == Role.ADMIN || viewer.getRole() == Role.SUPER_ADMIN);
        Long viewerId = viewer != null && viewer.getRole() == Role.ORGANIZER ? viewer.getId() : null;
        return eventRepository.search(normalize(q), categoryId, venueId, status, fromDate, toDate, admin, viewerId, pageable)
                .map(e -> toResponse(e));
    }

    @Transactional(readOnly = true)
    public EventResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public EventResponse getForViewer(Long id, String email) {
        Event event = getEntity(id);
        if (event.getStatus() != EventStatus.DRAFT) {
            return toResponse(event);
        }
        if (email == null) {
            throw new org.springframework.security.access.AccessDeniedException("Draft event is not publicly available");
        }
        User viewer = currentUser(email);
        boolean owner = viewer.getRole() == Role.ORGANIZER
                && event.getOrganizer().getId().equals(viewer.getId());
        boolean admin = viewer.getRole() == Role.ADMIN || viewer.getRole() == Role.SUPER_ADMIN;
        if (!owner && !admin) {
            throw new org.springframework.security.access.AccessDeniedException("Draft event is not publicly available");
        }
        return toResponse(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> mine(String email, int page, int size) {
        User user = currentUser(email);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "eventDate"));
        return eventRepository.findByOrganizerId(user.getId(), pageable).map(e -> toResponse(e));
    }

    @Transactional
    public EventResponse create(String email, EventRequest request) {
        User organizer = currentUser(email);
        if (!(organizer.getRole() == Role.ORGANIZER || organizer.getRole() == Role.ADMIN || organizer.getRole() == Role.SUPER_ADMIN)) {
            throw new IllegalArgumentException("You are not authorized to create events");
        }
        validateDates(request);
        Category category = categoryService.get(request.categoryId());
        Venue venue = venueService.get(request.venueId());
        if (!category.isActive()) throw new IllegalArgumentException("Category is inactive");
        if (!venue.isActive()) throw new IllegalArgumentException("Venue is inactive");
        Event event = Event.builder()
                .title(request.title().trim()).description(request.description().trim()).bannerImage(request.bannerImage())
                .category(category).venue(venue).organizer(organizer)
                .eventDate(request.eventDate()).startTime(request.startTime()).endTime(request.endTime())
                .registrationStart(request.registrationStart()).registrationDeadline(request.registrationDeadline())
                .maxAttendees(request.maxAttendees()).rules(request.rules())
                .contactEmail(request.contactEmail()).contactPhone(request.contactPhone()).status(EventStatus.DRAFT).build();
        Event saved=eventRepository.save(event); audit.log(organizer,"EVENT_CREATED","EVENT",saved.getId(),null,"Event created"); return toResponse(saved);
    }

    @Transactional
    public EventResponse update(String email, Long id, EventRequest request) {
        Event event = getEntity(id);
        authorizeMutation(email, event);
        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Completed or cancelled events cannot be edited");
        }
        validateDates(request);
        Category category = categoryService.get(request.categoryId());
        Venue venue = venueService.get(request.venueId());
        if (!category.isActive()) throw new IllegalArgumentException("Category is inactive");
        if (!venue.isActive()) throw new IllegalArgumentException("Venue is inactive");
        event.setTitle(request.title().trim()); event.setDescription(request.description().trim()); event.setBannerImage(request.bannerImage());
        event.setCategory(category); event.setVenue(venue); event.setEventDate(request.eventDate()); event.setStartTime(request.startTime());
        event.setEndTime(request.endTime()); event.setRegistrationStart(request.registrationStart()); event.setRegistrationDeadline(request.registrationDeadline());
        event.setMaxAttendees(request.maxAttendees()); event.setRules(request.rules()); event.setContactEmail(request.contactEmail()); event.setContactPhone(request.contactPhone());
        Event saved=eventRepository.save(event); User actor=currentUser(email); audit.log(actor,"EVENT_UPDATED","EVENT",id,null,"Event updated"); registrationRepository.findByEventIdAndStatusIn(id,List.of(RegistrationStatus.REGISTERED,RegistrationStatus.ATTENDED)).forEach(r->{notifications.send(r.getUser(),"Event updated",event.getTitle()+" has been updated.",NotificationType.EVENT_UPDATE);emailService.send(r.getUser().getEmail(),"EventHub event updated",event.getTitle()+" has been updated.");}); return toResponse(saved);
    }

    @Transactional
    public void delete(String email, Long id) {
        Event event = getEntity(id);
        authorizeMutation(email, event);
        if (event.getStatus() != EventStatus.DRAFT) throw new IllegalArgumentException("Only draft events can be deleted");
        eventRepository.delete(event); audit.log(currentUser(email),"EVENT_DELETED","EVENT",id,null,"Draft event deleted");
    }

    @Transactional
    public EventResponse changeStatus(String email, Long id, EventStatus target) {
        Event event = getEntity(id);
        authorizeMutation(email, event);
        validateTransition(event.getStatus(), target);
        event.setStatus(target);
        Event saved=eventRepository.save(event);
        User actor=currentUser(email);
        audit.log(actor,"EVENT_STATUS_CHANGED","EVENT",id,null,"Status changed to "+target);
        if(target==EventStatus.CANCELLED){registrationRepository.findByEventIdAndStatusIn(id,List.of(RegistrationStatus.REGISTERED,RegistrationStatus.ATTENDED)).forEach(r->{notifications.send(r.getUser(),"Event cancelled","The event "+event.getTitle()+" has been cancelled.",NotificationType.EVENT_CANCELLED);emailService.send(r.getUser().getEmail(),"EventHub event cancelled","The event "+event.getTitle()+" has been cancelled.");});}
        return toResponse(saved);
    }

    private EventResponse toResponse(Event event) {
        int count = (int) registrationRepository.countByEventIdAndStatusIn(event.getId(), java.util.List.of(RegistrationStatus.REGISTERED, RegistrationStatus.ATTENDED));
        return EventResponse.from(event, count);
    }

    private void validateDates(EventRequest r) {
        if (r.startTime().isAfter(r.endTime()) || r.startTime().equals(r.endTime())) throw new IllegalArgumentException("Start time must be before end time");
        if (r.registrationStart().isAfter(r.registrationDeadline())) throw new IllegalArgumentException("Registration start cannot be after deadline");
        if (r.registrationDeadline().isAfter(r.eventDate())) throw new IllegalArgumentException("Registration deadline cannot be after the event date");
    }

    private void validateTransition(EventStatus from, EventStatus to) {
        if (from == to) throw new IllegalArgumentException("Event is already in that status");
        boolean allowed = switch (from) {
            case DRAFT -> to == EventStatus.PUBLISHED || to == EventStatus.CANCELLED;
            case PUBLISHED -> to == EventStatus.REGISTRATION_OPEN || to == EventStatus.CANCELLED;
            case REGISTRATION_OPEN -> to == EventStatus.REGISTRATION_CLOSED || to == EventStatus.CANCELLED;
            case REGISTRATION_CLOSED -> to == EventStatus.ONGOING || to == EventStatus.CANCELLED;
            case ONGOING -> to == EventStatus.COMPLETED || to == EventStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
        if (!allowed) throw new IllegalArgumentException("Invalid event status transition from " + from + " to " + to);
    }

    private void authorizeMutation(String email, Event event) {
        User user = currentUser(email);
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN) return;
        if (user.getRole() != Role.ORGANIZER || !event.getOrganizer().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to modify this event");
        }
    }

    private User currentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Event getEntity(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    private Sort buildSort(String sortBy, String direction) {
        String field = switch (sortBy == null ? "date" : sortBy.toLowerCase()) {
            case "title" -> "title";
            case "created", "createdat" -> "createdAt";
            case "date", "eventdate" -> "eventDate";
            default -> throw new IllegalArgumentException("Unsupported sortBy. Use date, title or createdAt; popularity requires a dedicated aggregate query and is exposed separately from basic sorting");
        };
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, field);
    }

    private String normalize(String q) { return q == null || q.isBlank() ? null : q.trim(); }
}
