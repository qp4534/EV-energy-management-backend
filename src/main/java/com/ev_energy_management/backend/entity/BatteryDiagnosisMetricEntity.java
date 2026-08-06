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
@Table(name = "`BATTERY_DIAGNOSIS_METRICS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryDiagnosisMetricEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`metric_id`")
    private UUID metricId;

    @Column(name = "`remaining_life_score`")
    private Integer remainingLifeScore;

    @Column(name = "`discharge_power_score`")
    private Integer dischargePowerScore;

    @Column(name = "`charge_health_score`")
    private Integer chargeHealthScore;

    @Column(name = "`voltage_stability_score`")
    private Integer voltageStabilityScore;

    @Column(name = "`diagnosed_at`")
    private OffsetDateTime diagnosedAt;

    @Column(name = "`battery_id`", nullable = false)
    private UUID batteryId;

    @PrePersist
    protected void onCreate() {
        if (diagnosedAt == null) {
            diagnosedAt = OffsetDateTime.now();
        }
    }
}
