package com.sascom.chickenstock.domain.trade.dto.response;

import com.sascom.chickenstock.domain.account.entity.TradeStatus;
import com.sascom.chickenstock.domain.trade.dto.request.TradeRequest;
import lombok.Builder;

@Builder
public record TradeResponse(
        TradeStatus status
//        TradeRequest tradeRequest
) { }
