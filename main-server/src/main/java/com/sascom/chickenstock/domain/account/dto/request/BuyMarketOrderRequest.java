package com.sascom.chickenstock.domain.account.dto.request;

import com.sascom.chickenstock.domain.trade.dto.OrderType;
import com.sascom.chickenstock.domain.trade.dto.request.BuyTradeRequest;
import com.sascom.chickenstock.domain.trade.dto.request.SellTradeRequest;

import java.time.LocalDateTime;

// 시장가 구매 dto
// 필요한 필드: 회사id, 수량, 증거금률
public record BuyMarketOrderRequest(
        Long accountId,
        Long competitionId,
        Long memberId,
        Long companyId,
        Long unitCost,
        Long volume,
        Boolean margin
) {
    public BuyTradeRequest toBuyTradeRequestEntity(
            OrderType orderType) {
        return BuyTradeRequest.builder()
                .orderType(orderType)
                .accountId(accountId)
                .memberId(memberId)
                .companyId(companyId)
                .competitionId(competitionId)
                .unitCost(orderType == OrderType.LIMIT? unitCost : 0)
//                .totalOrderVolume(volume)
                .build();
    }
}
