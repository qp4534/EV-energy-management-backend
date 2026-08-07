package com.ev_energy_management.backend.dto.chat;

public record ChatSourceDto(
        String chunkId,
        String title,
        String sourceType,
        Integer page,
        String clause,
        String url,
        Double score
) {}
