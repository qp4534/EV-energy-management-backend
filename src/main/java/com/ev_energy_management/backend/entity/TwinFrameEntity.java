package com.ev_energy_management.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "`TWIN_FRAMES`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwinFrameEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`frame_id`")
    private UUID frameId;

    @Column(name = "`observed_at`", nullable = false)
    private OffsetDateTime observedAt;

    @Column(name = "`hotspot_cell_index`")
    private Short hotspotCellIndex;

    @Column(name = "`hotspot_connector_index`")
    private Short hotspotConnectorIndex;

    @Column(name = "`ml_risk_level`")
    private Short mlRiskLevel;

    @Column(name = "`physics_risk_level`")
    private Short physicsRiskLevel;

    @Column(name = "`final_risk_level`")
    private Short finalRiskLevel;

    @Column(name = "`image_risk_level`")
    private Short imageRiskLevel;

    @Column(name = "`image_confidence`")
    private Float imageConfidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "`raw_metrics`")
    private String rawMetrics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "`model_input`")
    private String modelInput;

    @Column(name = "`anomaly_id`")
    private UUID anomalyId;

    @Column(name = "`car_id`", nullable = false)
    private UUID carId;

    @Column(name = "`session_id`")
    private UUID sessionId;

    @Column(name = "`source_image_ref`")
    private String sourceImageRef;

    @PrePersist
    protected void onCreate() {
        if (observedAt == null) {
            observedAt = OffsetDateTime.now();
        }
    }
}
