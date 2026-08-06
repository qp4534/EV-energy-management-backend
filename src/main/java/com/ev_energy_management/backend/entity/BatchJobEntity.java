package com.ev_energy_management.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "`BATCH_JOBS`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchJobEntity {

    @Id
    @Column(name = "`job_id`")
    private String jobId;

    @Column(name = "`job_name`", nullable = false)
    private String jobName;

    @Column(name = "`cycle`")
    private String cycle;

    @Column(name = "`status`", nullable = false)
    private String status;

    @Column(name = "`last_run_at`")
    private OffsetDateTime lastRunAt;

    @Column(name = "`next_run_at`")
    private OffsetDateTime nextRunAt;
}
