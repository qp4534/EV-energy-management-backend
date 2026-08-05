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
@Table(name = "`CAR`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`car_id`")
    private UUID carId;

    @Column(name = "`car_number`", nullable = false)
    private String carNumber;

    @Column(name = "`model`", nullable = false)
    private String model;

    @Column(name = "`vin`", nullable = false)
    private String vin;

    @Column(name = "`nickname`")
    private String nickname;

    @Column(name = "`image_url`")
    private String imageUrl;

    @Column(name = "`is_primary`", nullable = false)
    private Boolean isPrimary;

    @Column(name = "`created_at`", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "`user_id`", nullable = false)
    private UUID userId;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
