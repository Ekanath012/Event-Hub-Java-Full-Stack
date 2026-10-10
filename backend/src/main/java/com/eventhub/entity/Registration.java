package com.eventhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="registrations", uniqueConstraints=@UniqueConstraint(name="uk_registration_event_user", columnNames={"event_id","user_id"}), indexes={
 @Index(name="idx_reg_event_status", columnList="event_id,status"), @Index(name="idx_reg_user", columnList="user_id"), @Index(name="idx_reg_number", columnList="registration_number", unique=true)})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Registration {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="registration_number",nullable=false,unique=true,length=40) private String registrationNumber;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id",nullable=false) private Event event;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(nullable=false,updatable=false) private Instant registeredAt;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) @Builder.Default private RegistrationStatus status=RegistrationStatus.REGISTERED;
 @OneToOne(mappedBy="registration",fetch=FetchType.LAZY) private Ticket ticket;
 @PrePersist void onCreate(){ if(registeredAt==null) registeredAt=Instant.now(); }
}
