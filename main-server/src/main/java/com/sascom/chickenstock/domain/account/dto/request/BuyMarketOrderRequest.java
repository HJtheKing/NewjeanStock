package com.sascom.chickenstock.domain.account.dto.request;

/**
 * 시장가 구매 dto
 */
public record BuyMarketOrderRequest(
        Long accountId,
        Long competitionId,
        Long memberId,
        Long companyId,
        Long unitCost,
        Long volume,
        Boolean margin
) {}
