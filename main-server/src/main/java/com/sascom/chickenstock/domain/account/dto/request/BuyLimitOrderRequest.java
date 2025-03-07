package com.sascom.chickenstock.domain.account.dto.request;

import com.sascom.chickenstock.domain.trade.dto.OrderType;
import com.sascom.chickenstock.domain.trade.dto.request.BuyTradeRequest;
import com.sascom.chickenstock.domain.trade.dto.request.SellTradeRequest;

import java.time.LocalDateTime;

// 지정가 매수 dto
// 필요한 필드: 회사id, 가격, 수량, 미수거래 여부
public record BuyLimitOrderRequest(
        Long accountId,
        Long memberId,
        Long companyId,
        Long competitionId,
        Long unitCost,
        Long volume,
        Boolean margin
) {
    // record가 final private, 변수 생성자, setter, hashCode, toString 등 자동 지정
    // 이후 세부 로직은 serivce에 구현

//    public BuyTradeRequest toBuyTradeRequestEntity(
//            OrderType orderType) {
//        return BuyTradeRequest.builder()
//                .orderType(orderType)
//                .accountId(accountId)
//                .memberId(memberId)
//                .companyId(companyId)
//                .competitionId(competitionId)
//                .unitCost(orderType == OrderType.LIMIT? unitCost : 0)
//                .totalOrderVolume(volume)
//                .build();
//    }
}
