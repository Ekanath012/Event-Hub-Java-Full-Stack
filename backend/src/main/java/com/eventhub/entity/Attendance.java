package com.eventhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="attendance",uniqueConstraints=@UniqueConstraint(name="uk_attendance_registration",columnNames="registration_id"),indexes=@Index(name="idx_attendance_event",columnList="event_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="registration_id",nullable=false,unique=true) private Registration registration;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id",nullable=false) private Event event;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="checked_in_by",nullable=false) private User checkedInBy;
 @Column(nullable=false) private Instant checkedInAt;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) @Builder.Default private AttendanceStatus status=AttendanceStatus.CHECKED_IN;
 @PrePersist void onCreate(){if(checkedInAt==null) checkedInAt=Instant.now();}
}
