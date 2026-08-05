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
@Table(name = "`NOTIFICATION_CHANNELS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationChannelEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`channel_id`")
    private UUID channelId;

    @Column(name = "`channel_name`", nullable = false)
    private String channelName;

    @Column(name = "`is_active`", nullable = false)
    private Boolean isActive;

    @Column(name = "`updated_at`")
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = OffsetDateTime.now();
    }
}
