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
@Table(name = "`ANOMALY_LOGS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyLogEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`anomaly_id`")
    private UUID anomalyId;

    @Column(name = "`abnormal_type`", nullable = false)
    private String abnormalType;

    @Column(name = "`source_type`", nullable = false)
    private String sourceType;

    @Column(name = "`trigger_value`")
    private String triggerValue;

    @Column(name = "`detected_at`")
    private OffsetDateTime detectedAt;

    @Column(name = "`risk_level`", nullable = false)
    private String riskLevel;

    @Column(name = "`car_id`")
    private UUID carId;

    @Column(name = "`session_id`")
    private UUID sessionId;

    @PrePersist
    protected void onCreate() {
        if (detectedAt == null) {
            detectedAt = OffsetDateTime.now();
        }
    }
}
