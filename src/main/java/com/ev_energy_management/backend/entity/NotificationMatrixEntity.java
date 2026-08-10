package com.ev_energy_management.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "`NOTIFICATION_MATRIX`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMatrixEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`matrix_id`")
    private UUID matrixId;

    @Column(name = "`risk_level`", nullable = false)
    private String riskLevel;

    @Column(name = "`is_enabled`", nullable = false)
    private Boolean isEnabled;

    @Column(name = "`channel_id`", nullable = false)
    private String channelId;
}
