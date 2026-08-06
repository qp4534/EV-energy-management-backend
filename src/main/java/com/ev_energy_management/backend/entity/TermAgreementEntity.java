package com.ev_energy_management.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "`TERM_AGREEMENT`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermAgreementEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`agreement_id`")
    private UUID agreementId;

    @Column(name = "`term_key`", nullable = false)
    private String termKey;

    @Column(name = "`agreed_at`", updatable = false)
    private OffsetDateTime agreedAt;

    @Column(name = "`user_id`", nullable = false)
    private UUID userId;

    @PrePersist
    protected void onCreate() {
        if (agreedAt == null) {
            agreedAt = OffsetDateTime.now();
        }
    }
}
