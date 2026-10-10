package com.eventhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="waitlist_entries", uniqueConstraints=@UniqueConstraint(name="uk_waitlist_event_user",columnNames={"event_id","user_id"}), indexes={@Index(name="idx_waitlist_event_position",columnList="event_id,position"),@Index(name="idx_waitlist_user",columnList="user_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WaitlistEntry {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id",nullable=false) private Event event;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(nullable=false) private Integer position;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) @Builder.Default private WaitlistStatus status=WaitlistStatus.WAITING;
 @Column(nullable=false,updatable=false) private Instant joinedAt;
 @PrePersist void onCreate(){if(joinedAt==null) joinedAt=Instant.now();}
}
