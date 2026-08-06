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
@Table(name = "`LOGIN_LOGS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginLogEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`log_id`")
    private UUID logId;

    @Column(name = "`ip_address`", nullable = false)
    private String ipAddress;

    @Column(name = "`user_agent`", nullable = false)
    private String userAgent;

    @Column(name = "`location`")
    private String location;

    @Column(name = "`status`", nullable = false)
    private String status;

    @Column(name = "`created_at`", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "`user_id`", nullable = false)
    private UUID userId;

    @Column(name = "`fail_reason`")
    private String failReason;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
