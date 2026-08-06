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
@Table(name = "`BATTERY_PROPOSALS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryProposalEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`proposal_id`")
    private UUID proposalId;

    @Column(name = "`total_price`", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "`price_per_kwh`", nullable = false)
    private BigDecimal pricePerKwh;

    @Column(name = "`capacity_range`")
    private String capacityRange;

    @Column(name = "`suitability_reason`")
    private String suitabilityReason;

    @Column(name = "`notice_text`")
    private String noticeText;

    @Column(name = "`created_at`", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "`battery_id`", nullable = false)
    private UUID batteryId;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
