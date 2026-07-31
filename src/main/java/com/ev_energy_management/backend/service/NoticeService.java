package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NoticeDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NoticeService {

    private List<NoticeDto> mockData() {
        return List.of(
                new NoticeDto(UUID.randomUUID(), "정기 점검 안내", "이번 주 토요일 시스템 점검이 있습니다.",
                        true, OffsetDateTime.now(), UUID.randomUUID(), false),
                new NoticeDto(UUID.randomUUID(), "신규 기능 안내", "배터리 진단 리포트 기능이 추가되었습니다.",
                        false, OffsetDateTime.now(), UUID.randomUUID(), true)
        );
    }

    public List<NoticeDto> findAll() {
        return mockData();
    }

    public NoticeDto findById(UUID noticeId) {
        return new NoticeDto(noticeId, "정기 점검 안내", "이번 주 토요일 시스템 점검이 있습니다.",
                true, OffsetDateTime.now(), UUID.randomUUID(), false);
    }

    public NoticeDto create(NoticeDto request) {
        return new NoticeDto(UUID.randomUUID(), request.title(), request.content(), request.isPinned(),
                OffsetDateTime.now(), request.userId(), false);
    }

    public NoticeDto update(UUID noticeId, NoticeDto request) {
        return new NoticeDto(noticeId, request.title(), request.content(), request.isPinned(),
                request.createdAt(), request.userId(), request.isRead());
    }

    public void delete(UUID noticeId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
