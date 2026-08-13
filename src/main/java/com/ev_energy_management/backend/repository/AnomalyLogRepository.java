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

    // generate_series로 "오늘 포함 최근 5일" 날짜 뼈대를 먼저 만들고 LEFT JOIN해서, 그날
    // 이상징후가 하나도 없어도(al.car_id가 전부 NULL) COUNT(DISTINCT ...)가 0으로 나오게
    // 한다 - 예전엔 GROUP BY만 써서 이상징후가 있었던 날짜만 나왔고, 그래서 "최근 5일"이
    // 아니라 "이상징후가 있었던 최근 5개 날짜"가 되는 문제가 있었다.
    @Query(value = """
            SELECT TO_CHAR(day, 'YYYY-MM-DD') AS "date",
                   COUNT(DISTINCT al.car_id) AS "count"
            FROM generate_series(
                     DATE_TRUNC('day', now()) - INTERVAL '4 days',
                     DATE_TRUNC('day', now()),
                     INTERVAL '1 day'
                 ) AS day
            LEFT JOIN public."ANOMALY_LOGS" al
                   ON DATE_TRUNC('day', al.detected_at) = day
                  AND al.risk_level IN ('주의', '경고', '긴급')
            GROUP BY day
            ORDER BY day ASC
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
