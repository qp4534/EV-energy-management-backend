package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ResourceUsageDto;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;

// 시스템 관리 "시스템 상태"/"리소스 사용률" 탭 전용.
// 별도 테이블 없이, 호출되는 그 순간의 서버(JVM/OS) 상태를 그대로 읽어서 보여줌.
// 그래서 과거 이력 조회는 불가능하고, 항상 "지금 이 순간" 값만 나옴.
@Service
public class SystemMonitorService {

    public List<ResourceUsageDto> getResourceUsage() {
        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        int cpuPercent = (int) Math.round(Math.max(0, osBean.getCpuLoad()) * 100);

        long totalMemory = osBean.getTotalMemorySize();
        long freeMemory = osBean.getFreeMemorySize();
        int memoryPercent = totalMemory == 0 ? 0
                : (int) Math.round((double) (totalMemory - freeMemory) / totalMemory * 100);

        File root = new File("/");
        long totalDisk = root.getTotalSpace();
        long usableDisk = root.getUsableSpace();
        int diskPercent = totalDisk == 0 ? 0
                : (int) Math.round((double) (totalDisk - usableDisk) / totalDisk * 100);

        return List.of(
                new ResourceUsageDto("cpu", "CPU", cpuPercent),
                new ResourceUsageDto("memory", "메모리", memoryPercent),
                new ResourceUsageDto("disk", "디스크", diskPercent)
        );
    }
}