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
@Table(name = "`BATCH_JOB_LOGS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchJobLogEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`batch_log_id`")
    private UUID batchLogId;

    @Column(name = "`run_type`", nullable = false)
    private String runType;

    @Column(name = "`status`", nullable = false)
    private String status;

    @Column(name = "`message`")
    private String message;

    @Column(name = "`executed_at`")
    private OffsetDateTime executedAt;

    @Column(name = "`job_id`", nullable = false)
    private String jobId;
}
