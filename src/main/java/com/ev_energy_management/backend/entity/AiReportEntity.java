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
@Table(name = "`AI_REPORTS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiReportEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`report_id`")
    private UUID reportId;

    @Column(name = "`title`", nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "`report_data`", nullable = false)
    private String reportData;

    @Column(name = "`report_type`", nullable = false)
    private String reportType;

    @Column(name = "`created_at`", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "`car_id`")
    private UUID carId;

    @Column(name = "`anomaly_id`")
    private UUID anomalyId;

    @Column(name = "`is_read`", nullable = false)
    private Boolean isRead;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
