package com.sascom.chickenstock.domain.account.dto.response;

import com.sascom.chickenstock.domain.trade.dto.OrderType;
import com.sascom.chickenstock.domain.trade.dto.TradeType;

public record UnexecutedStockInfoV2(
        Long companyId,
        Long price,
        Long volume,
        OrderType orderType,
        TradeType tradeType,
        Long orderId
) {
}
