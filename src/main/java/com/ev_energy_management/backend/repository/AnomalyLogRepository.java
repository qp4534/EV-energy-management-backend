package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.AnomalyLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AnomalyLogRepository extends JpaRepository<AnomalyLogEntity, UUID> {

    @Query(value = """
            SELECT c.car_id AS "carId",
                   c.car_number AS "carNumber",
                   c.model AS "model",
                   c.vin AS "vin",
                   COALESCE(latest.risk_level, '정상') AS "riskLevel",
                   latest.abnormal_type AS "abnormalType",
                   latest.detected_at AS "detectedAt"
            FROM public."CAR" c
            LEFT JOIN LATERAL (
                SELECT al.risk_level, al.abnormal_type, al.detected_at
                FROM public."ANOMALY_LOGS" al
                WHERE al.car_id = c.car_id
                ORDER BY al.detected_at DESC NULLS LAST, al.anomaly_id DESC
                LIMIT 1
            ) latest ON TRUE
            ORDER BY c.car_number ASC
            """, nativeQuery = true)
    List<VehicleRiskProjection> findLatestRiskByCar();

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('day', detected_at), 'YYYY-MM-DD') AS "date",
                   COUNT(DISTINCT car_id) AS "count"
            FROM public."ANOMALY_LOGS"
            WHERE risk_level IN ('주의', '경고', '긴급')
              AND detected_at IS NOT NULL
            GROUP BY DATE_TRUNC('day', detected_at)
            ORDER BY DATE_TRUNC('day', detected_at) DESC
            LIMIT 5
            """, nativeQuery = true)
    List<DailyRiskCountProjection> findRecentDailyRiskCounts();

    interface VehicleRiskProjection {
        UUID getCarId();
        String getCarNumber();
        String getModel();
        String getVin();
        String getRiskLevel();
        String getAbnormalType();
        Instant getDetectedAt();
    }

    interface DailyRiskCountProjection {
        String getDate();
        Long getCount();
    }
}
