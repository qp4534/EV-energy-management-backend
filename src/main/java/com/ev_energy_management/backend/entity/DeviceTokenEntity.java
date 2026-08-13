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
@Table(name = "`DEVICE_TOKENS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceTokenEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`device_token_id`")
    private UUID deviceTokenId;

    @Column(name = "`user_id`", nullable = false)
    private UUID userId;

    @Column(name = "`expo_push_token`", nullable = false)
    private String expoPushToken;

    @Column(name = "`platform`")
    private String platform;

    @Column(name = "`created_at`", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "`updated_at`")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
