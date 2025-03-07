package com.sascom.chickenstock.domain.account.dto.request;

import com.sascom.chickenstock.domain.trade.dto.OrderType;
import com.sascom.chickenstock.domain.trade.dto.request.BuyTradeRequest;
import com.sascom.chickenstock.domain.trade.dto.request.SellTradeRequest;

import java.time.LocalDateTime;

// 시장가 매도 dto
// 필요한 필드: 회사id, 수량
public record SellMarketOrderRequest(
        Long accountId,
        Long competitionId,
        Long memberId,
        Long companyId,
        Long unitCost,
        Long volume
) {

    public SellTradeRequest toSellTradeRequestEntity(
            OrderType orderType) {
        return SellTradeRequest.builder()
                .orderType(orderType)
                .accountId(accountId)
                .memberId(memberId)
                .companyId(companyId)
                .competitionId(competitionId)
                .unitCost(orderType == OrderType.LIMIT? unitCost : 0)
                .totalOrderVolume(volume)
                .build();
    }
}
