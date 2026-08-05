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
@Table(name = "`CHARGING_SESSION`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingSessionEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`session_id`")
    private UUID sessionId;

    @Column(name = "`start_time`")
    private OffsetDateTime startTime;

    @Column(name = "`end_time`")
    private OffsetDateTime endTime;

    @Column(name = "`change_state`", nullable = false)
    private String changeState;

    @Column(name = "`car_id`", nullable = false)
    private UUID carId;

    @Column(name = "`charger_id`", nullable = false)
    private UUID chargerId;

    @Column(name = "`start_soc`")
    private BigDecimal startSoc;

    @Column(name = "`end_soc`")
    private BigDecimal endSoc;
}
