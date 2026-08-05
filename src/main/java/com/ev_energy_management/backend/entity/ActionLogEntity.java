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
@Table(name = "`ACTION_LOGS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionLogEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`action_id`")
    private UUID actionId;

    @Column(name = "`action_type`", nullable = false)
    private String actionType;

    @Column(name = "`target_type`")
    private String targetType;

    @Column(name = "`target_id`", nullable = false)
    private UUID targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "`detail`")
    private String detail;

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
