package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NotificationChannelDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class NotificationChannelService {

    private List<NotificationChannelDto> mockData() {
        return List.of(
                new NotificationChannelDto("SLACK", "Slack 알림", true, OffsetDateTime.now()),
                new NotificationChannelDto("SMS", "SMS 알림", false, OffsetDateTime.now())
        );
    }

    public List<NotificationChannelDto> findAll() {
        return mockData();
    }

    public NotificationChannelDto findById(String channelId) {
        return new NotificationChannelDto(channelId, "Slack 알림", true, OffsetDateTime.now());
    }

    public NotificationChannelDto create(NotificationChannelDto request) {
        return new NotificationChannelDto(request.channelId(), request.channelName(), request.isActive(), OffsetDateTime.now());
    }

    public NotificationChannelDto update(String channelId, NotificationChannelDto request) {
        return new NotificationChannelDto(channelId, request.channelName(), request.isActive(), OffsetDateTime.now());
    }

    public void delete(String channelId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
