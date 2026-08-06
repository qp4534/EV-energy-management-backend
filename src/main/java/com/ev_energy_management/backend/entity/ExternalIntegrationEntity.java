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
@Table(name = "`EXTERNAL_INTEGRATIONS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalIntegrationEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`integration_id`")
    private UUID integrationId;

    @Column(name = "`name`", nullable = false)
    private String name;

    @Column(name = "`description`")
    private String description;

    @Column(name = "`api_key`", nullable = false)
    private String apiKey;

    @Column(name = "`is_status`", nullable = false)
    private Boolean isStatus;

    @Column(name = "`last_connected_at`")
    private OffsetDateTime lastConnectedAt;

    @Column(name = "`created_at`", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
