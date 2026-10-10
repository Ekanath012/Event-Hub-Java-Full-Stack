package com.eventhub.entity;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="audit_logs",indexes={@Index(name="idx_audit_timestamp",columnList="timestamp"),@Index(name="idx_audit_user",columnList="user_id"),@Index(name="idx_audit_action",columnList="action"),@Index(name="idx_audit_entity",columnList="entity_type,entity_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(name="user_id") Long userId; @Column(nullable=false,length=80) String action; @Column(name="entity_type",length=80) String entityType; @Column(name="entity_id") Long entityId; @Column(name="ip_address",length=64) String ipAddress; @Column(name="timestamp",nullable=false) Instant timestamp; @Column(columnDefinition="TEXT") String details; @PrePersist void create(){if(timestamp==null)timestamp=Instant.now();}}
