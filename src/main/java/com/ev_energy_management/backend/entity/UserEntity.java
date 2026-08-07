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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "`USER`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`user_id`")
    private UUID userId;

    @Column(name = "`email`", nullable = false)
    private String email;

    @Column(name = "`password_hash`", nullable = false)
    private String passwordHash;

    @Column(name = "`role`", nullable = false)
    private String role;

    @Column(name = "`birth`", nullable = false)
    private LocalDate birth;

    @Column(name = "`is_agree`", nullable = false)
    private Boolean isAgree;

    @Column(name = "`login_faild`", nullable = false)
    private Integer loginFailed;

    @Column(name = "`is_locked`", nullable = false)
    private Boolean isLocked;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "`permissions`", nullable = false)
    private String permissions;

    @Column(name = "`name`", nullable = false)
    private String name;

    @Column(name = "`phone`", nullable = false)
    private String phone;

    @Column(name = "`profile_image_url`")
    private String profileImageUrl;

    @Column(name = "`created_at`", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "`email_verified`", nullable = false)
    private Boolean emailVerified;

    @Column(name = "`locked_at`")
    private OffsetDateTime lockedAt;

    @Column(name = "`is_deleted`", nullable = false)
    private Boolean isDeleted;

    @Column(name = "`deleted_at`")
    private OffsetDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
}
