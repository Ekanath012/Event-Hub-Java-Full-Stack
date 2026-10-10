package com.eventhub.entity;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="feedback",uniqueConstraints=@UniqueConstraint(name="uk_feedback_event_user",columnNames={"event_id","user_id"}),indexes={@Index(name="idx_feedback_event",columnList="event_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Feedback { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id") Event event; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id") User user; @Column(nullable=false) int rating; @Column(length=2000) String comment; @Column(nullable=false,updatable=false) Instant createdAt; @PrePersist void create(){if(createdAt==null)createdAt=Instant.now();}}
