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
@Table(name = "`NOTICE`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`notice_id`")
    private UUID noticeId;

    @Column(name = "`title`", nullable = false)
    private String title;

    @Column(name = "`content`", nullable = false)
    private String content;

    @Column(name = "`is_pinned`", nullable = false)
    private Boolean isPinned;

    @Column(name = "`created_at`", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "`user_id`", nullable = false)
    private UUID userId;

    @Column(name = "`is_read`", nullable = false)
    private Boolean isRead;

    @Column(name = "`is_important`", nullable = false)
    private Boolean isImportant;

    @Column(name = "`target_role`")
    private String targetRole;

    @Column(name = "`view_count`", nullable = false)
    private Integer viewCount;

    @Column(name = "`updated_at`")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
