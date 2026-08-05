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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "`BATTERY_PASSPORT`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryPassportEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`battery_id`")
    private UUID batteryId;

    @Column(name = "`manufacturer`")
    private String manufacturer;

    @Column(name = "`battery_type`")
    private String batteryType;

    @Column(name = "`rated_capacity`")
    private String ratedCapacity;

    @Column(name = "`soh_score`")
    private BigDecimal sohScore;

    @Column(name = "`charge_cycles`")
    private Integer chargeCycles;

    @Column(name = "`current_temp`")
    private BigDecimal currentTemp;

    @Column(name = "`last_inspected_at`")
    private LocalDate lastInspectedAt;

    @Column(name = "`car_id`", nullable = false, unique = true)
    private UUID carId;

    @Column(name = "`battery_level`")
    private String batteryLevel;

    @Column(name = "`remaining_cycles`")
    private Integer remainingCycles;

    @Column(name = "`total_cycles`")
    private Integer totalCycles;

    @Column(name = "`reuse_status`")
    private String reuseStatus;

    @Column(name = "`grade_detail`")
    private String gradeDetail;

    @Column(name = "`reliability_score`")
    private BigDecimal reliabilityScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "`reuse_probabilities`")
    private String reuseProbabilities;

    @Column(name = "`voltage`")
    private BigDecimal voltage;

    @Column(name = "`current`")
    private BigDecimal current;

    @Column(name = "`rul`")
    private BigDecimal rul;

    @Column(name = "`manufactured_at`")
    private LocalDate manufacturedAt;

    @Column(name = "`installed_at`")
    private LocalDate installedAt;
}
