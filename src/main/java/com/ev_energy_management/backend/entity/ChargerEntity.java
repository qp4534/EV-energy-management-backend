package com.ev_energy_management.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "`CHARGER`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargerEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`charger_id`")
    private UUID chargerId;

    @Column(name = "`charger_type`", nullable = false)
    private String chargerType;

    @Column(name = "`rated_power_kw`")
    private BigDecimal ratedPowerKw;

    @Column(name = "`status`", nullable = false)
    private String status;

    @Column(name = "`queue_length`")
    private Integer queueLength;

    @Column(name = "`waiting_time_min`")
    private Integer waitingTimeMin;

    @Column(name = "`updated_at`")
    private OffsetDateTime updatedAt;

    @Column(name = "`charge_id`", nullable = false)
    private UUID chargeId;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = OffsetDateTime.now();
    }
}
