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
@Table(name = "`BATTERY_OFFERS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryOfferEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`offer_id`")
    private UUID offerId;

    @Column(name = "`buyer_name`", nullable = false)
    private String buyerName;

    @Column(name = "`business_type`")
    private String businessType;

    @Column(name = "`offered_price`", nullable = false)
    private BigDecimal offeredPrice;

    @Column(name = "`price_per_kwh`")
    private BigDecimal pricePerKwh;

    @Column(name = "`rank_order`")
    private Integer rankOrder;

    @Column(name = "`description`")
    private String description;

    @Column(name = "`battery_id`", nullable = false)
    private UUID batteryId;
}
