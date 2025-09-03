package com.sascom.chickenstock.domain.account.dto.request;

/**
 * 지정가 매도 dto
 */
public record SellLimitOrderRequest(
        Long accountId,
        Long competitionId,
        Long memberId,
        Long companyId,
        Long unitCost,
        Long volume
) {}
