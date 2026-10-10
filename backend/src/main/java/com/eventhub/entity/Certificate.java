package com.eventhub.entity;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="certificates",uniqueConstraints={@UniqueConstraint(name="uk_certificate_id",columnNames="certificate_id"),@UniqueConstraint(name="uk_certificate_registration",columnNames="registration_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Certificate { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(name="certificate_id",nullable=false,length=60,unique=true) String certificateId; @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="registration_id",unique=true) Registration registration; @Column(nullable=false,updatable=false) Instant issuedAt; @PrePersist void create(){if(issuedAt==null)issuedAt=Instant.now();}}
