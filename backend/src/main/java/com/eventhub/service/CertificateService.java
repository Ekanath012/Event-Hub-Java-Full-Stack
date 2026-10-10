
package com.eventhub.service;

import com.eventhub.dto.CertificateResponse;
import com.eventhub.entity.*;
import com.eventhub.repository.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certs;
    private final RegistrationRepository registrations;
    private final UserRepository users;
    private final EventRepository events;
    private final NotificationService notifications;
    private final AuditLogService audit;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String base;

    @Transactional
    public CertificateResponse generate(String email, Long registrationId, String ip) {

        User u = user(email);

        Registration r = registrations.findDetailed(registrationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Registration not found"));

        boolean self = r.getUser().getId().equals(u.getId());

        boolean admin = u.getRole() == Role.ADMIN
                || u.getRole() == Role.SUPER_ADMIN;

        boolean organizer = u.getRole() == Role.ORGANIZER
                && r.getEvent().getOrganizer().getId().equals(u.getId());

        if (!self && !admin && !organizer) {
            throw new AccessDeniedException("Not authorized");
        }

        if (r.getStatus() != RegistrationStatus.ATTENDED) {
            throw new IllegalArgumentException(
                    "Certificate requires attendance");
        }

        if (r.getEvent().getStatus() != EventStatus.COMPLETED) {
            throw new IllegalArgumentException("Event must be completed");
        }

        Optional<Certificate> existing =
                certs.findByRegistrationId(r.getId());

        Certificate c = existing.orElseGet(() ->
                certs.save(
                        Certificate.builder()
                                .certificateId(
                                        "CERT-" + UUID.randomUUID()
                                                .toString()
                                                .replace("-", "")
                                                .substring(0, 20)
                                                .toUpperCase()
                                )
                                .registration(r)
                                .build()
                )
        );

        if (existing.isEmpty()) {
            notifications.send(
                    r.getUser(),
                    "Certificate available",
                    "Your certificate for "
                            + r.getEvent().getTitle()
                            + " is ready.",
                    NotificationType.CERTIFICATE
            );

            audit.log(
                    u,
                    "CERTIFICATE_GENERATED",
                    "CERTIFICATE",
                    c.getId(),
                    ip,
                    "Certificate generated"
            );
        }

        return CertificateResponse.from(c, base);
    }

    @Transactional(readOnly = true)
    public CertificateResponse get(String email, Long id) {

        Certificate c = certs.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Certificate not found"));

        User u = user(email);

        if (!c.getRegistration().getUser().getId().equals(u.getId())
                && u.getRole() != Role.ADMIN
                && u.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Not authorized");
        }

        return CertificateResponse.from(c, base);
    }

    @Transactional(readOnly = true)
    public CertificateResponse verify(String certificateId) {

        return certs.findByCertificateId(certificateId)
                .map(c -> CertificateResponse.from(c, base))
                .orElseThrow(() ->
                        new IllegalArgumentException("Certificate not found"));
    }

    @Transactional(readOnly = true)
    public byte[] pdf(String email, Long id) throws IOException {

        Certificate c = certs.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Certificate not found"));

        User u = user(email);

        if (!c.getRegistration().getUser().getId().equals(u.getId())
                && u.getRole() != Role.ADMIN
                && u.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Not authorized");
        }

        return buildPdf(c);
    }

    private byte[] buildPdf(Certificate c) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document d = new Document(PageSize.A4.rotate());

        try {
            PdfWriter.getInstance(d, out);
            d.open();

            d.add(new Paragraph(
                    "EVENTHUB",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28)
            ));

            d.add(new Paragraph(
                    "Certificate of Participation",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22)
            ));

            d.add(new Paragraph(
                    "\nThis certifies that",
                    FontFactory.getFont(FontFactory.HELVETICA, 16)
            ));

            d.add(new Paragraph(
                    c.getRegistration().getUser().getFirstName()
                            + " "
                            + c.getRegistration().getUser().getLastName(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26)
            ));

            d.add(new Paragraph(
                    "\nhas successfully attended",
                    FontFactory.getFont(FontFactory.HELVETICA, 16)
            ));

            d.add(new Paragraph(
                    c.getRegistration().getEvent().getTitle(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22)
            ));

            d.add(new Paragraph(
                    "\nEvent date: "
                            + c.getRegistration().getEvent().getEventDate()
                            + "    Certificate ID: "
                            + c.getCertificateId(),
                    FontFactory.getFont(FontFactory.HELVETICA, 13)
            ));

            d.add(new Paragraph(
                    "\nIssued: " + c.getIssuedAt(),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)
            ));

            d.add(new Paragraph(
                    "\nVerification: "
                            + base
                            + "/api/certificates/verify/"
                            + c.getCertificateId(),
                    FontFactory.getFont(FontFactory.HELVETICA, 11)
            ));

        } finally {
            d.close();
        }

        return out.toByteArray();
    }

    private User user(String e) {

        return users.findByEmailIgnoreCase(e)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }
}
