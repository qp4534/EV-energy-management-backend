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
@Table(name = "`NOTIFICATIONS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`notification_id`")
    private UUID notificationId;

    @Column(name = "`risk_level`", nullable = false)
    private String riskLevel;

    @Column(name = "`title`", nullable = false)
    private String title;

    @Column(name = "`body`")
    private String body;

    @Column(name = "`is_read`", nullable = false)
    private Boolean isRead;

    @Column(name = "`created_at`", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "`user_id`", nullable = false)
    private UUID userId;

    @Column(name = "`car_id`")
    private UUID carId;

    @Column(name = "`report_id`")
    private UUID reportId;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}
