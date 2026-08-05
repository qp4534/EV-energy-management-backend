package com.ev_energy_management.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "`CHARGING_STATION`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingStationEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`charge_id`")
    private UUID chargeId;

    @Column(name = "`region`", nullable = false)
    private String region;

    @Column(name = "`address`", nullable = false)
    private String address;

    @Column(name = "`latitude`", nullable = false)
    private BigDecimal latitude;

    @Column(name = "`longitude`", nullable = false)
    private BigDecimal longitude;

    @Column(name = "`name`", nullable = false)
    private String name;

    @Column(name = "`slow_charger_count`", nullable = false)
    private Integer slowChargerCount;

    @Column(name = "`fast_charger_count`", nullable = false)
    private Integer fastChargerCount;

    @Column(name = "`available_count`", nullable = false)
    private Integer availableCount;

    @Column(name = "`min_queue_length`")
    private Integer minQueueLength;

    @Column(name = "`min_waiting_time`")
    private Integer minWaitingTime;
}
