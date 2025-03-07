package com.sascom.chickenstock.domain.account.dto.request;

public record CancelOrderRequestV2(
        Long accountId,
        Long memberId,
        Long competitionId,
        Long orderId
) {
}
