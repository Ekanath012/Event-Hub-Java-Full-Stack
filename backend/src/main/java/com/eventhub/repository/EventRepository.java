package com.eventhub.repository;

import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query(value = """
        select e from Event e
        join e.category c
        join e.venue v
        where (:q is null or lower(e.title) like lower(concat('%', :q, '%'))
               or lower(e.description) like lower(concat('%', :q, '%')))
          and (:categoryId is null or c.id = :categoryId)
          and (:venueId is null or v.id = :venueId)
          and (:includeAllDrafts = true or e.status <> com.eventhub.entity.EventStatus.DRAFT
               or (:viewerId is not null and e.organizer.id = :viewerId))
          and (:status is null or e.status = :status)
          and (:fromDate is null or e.eventDate >= :fromDate)
          and (:toDate is null or e.eventDate <= :toDate)
        """, countQuery = """
        select count(e) from Event e
        join e.category c
        join e.venue v
        where (:q is null or lower(e.title) like lower(concat('%', :q, '%'))
               or lower(e.description) like lower(concat('%', :q, '%')))
          and (:categoryId is null or c.id = :categoryId)
          and (:venueId is null or v.id = :venueId)
          and (:includeAllDrafts = true or e.status <> com.eventhub.entity.EventStatus.DRAFT
               or (:viewerId is not null and e.organizer.id = :viewerId))
          and (:status is null or e.status = :status)
          and (:fromDate is null or e.eventDate >= :fromDate)
          and (:toDate is null or e.eventDate <= :toDate)
        """)
    Page<Event> search(@Param("q") String q,
                       @Param("categoryId") Long categoryId,
                       @Param("venueId") Long venueId,
                       @Param("status") EventStatus status,
                       @Param("fromDate") LocalDate fromDate,
                       @Param("toDate") LocalDate toDate,
                       @Param("includeAllDrafts") boolean includeAllDrafts,
                       @Param("viewerId") Long viewerId,
                       Pageable pageable);

    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);
 long countByStatus(EventStatus status);
 long countByEventDateGreaterThanEqualAndStatusNot(java.time.LocalDate date, EventStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    java.util.Optional<Event> findByIdForUpdate(@Param("id") Long id);

    boolean existsByCategoryId(Long categoryId);
    boolean existsByVenueId(Long venueId);
}
