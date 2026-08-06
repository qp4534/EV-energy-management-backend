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
@Table(name = "`NOTICE_ATTACHMENT`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeAttachmentEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`attachment_id`")
    private UUID attachmentId;

    @Column(name = "`file_name`", nullable = false)
    private String fileName;

    @Column(name = "`file_url`", nullable = false)
    private String fileUrl;

    @Column(name = "`file_size`")
    private Long fileSize;

    @Column(name = "`file_type`")
    private String fileType;

    @Column(name = "`notice_id`", nullable = false)
    private UUID noticeId;
}
