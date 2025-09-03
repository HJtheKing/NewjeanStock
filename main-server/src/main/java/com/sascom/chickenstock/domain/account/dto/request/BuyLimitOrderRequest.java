package com.sascom.chickenstock.domain.account.dto.request;

/**
 * 지정가 매수 dto
 */
public record BuyLimitOrderRequest(
        Long accountId,
        Long memberId,
        Long companyId,
        Long competitionId,
        Long unitCost,
        Long volume,
        Boolean margin
) {}
