package com.eventhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="tickets",uniqueConstraints=@UniqueConstraint(name="uk_ticket_public_id",columnNames="ticket_id"),indexes=@Index(name="idx_ticket_token_hash",columnList="secure_token_hash"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="ticket_id",nullable=false,unique=true,length=50) private String ticketId;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="registration_id",nullable=false,unique=true) private Registration registration;
 @Column(name="secure_token_hash",nullable=false,length=64,columnDefinition="CHAR(64)") private String secureTokenHash;
 @Column(nullable=false,updatable=false) private Instant issuedAt;
 @PrePersist void onCreate(){if(issuedAt==null) issuedAt=Instant.now();}
}
