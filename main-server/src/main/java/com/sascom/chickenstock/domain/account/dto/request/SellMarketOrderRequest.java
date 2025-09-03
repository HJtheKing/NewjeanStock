package com.sascom.chickenstock.domain.account.dto.request;

/**
 * 시장가 매도 dto
 */
public record SellMarketOrderRequest(
        Long accountId,
        Long competitionId,
        Long memberId,
        Long companyId,
        Long unitCost,
        Long volume
) {}
