package com.sascom.chickenstock.domain.competition.dto.response;

import com.sascom.chickenstock.domain.history.entity.HistoryStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CompetitionHistoryResponse(
        String companyName,
        Long price,
        Long quantity,
        HistoryStatus status,
        LocalDateTime createdAt
) {
}
