package com.eventhub.entity;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="notifications", indexes={@Index(name="idx_notifications_user",columnList="user_id"),@Index(name="idx_notifications_created",columnList="created_at")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id") User user; @Column(nullable=false,length=160) String title; @Column(nullable=false,length=1000) String message; @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) NotificationType type; @Column(name="is_read",nullable=false) @Builder.Default boolean read=false; @Column(name="created_at",nullable=false,updatable=false) Instant createdAt; @PrePersist void create(){if(createdAt==null)createdAt=Instant.now();}}
