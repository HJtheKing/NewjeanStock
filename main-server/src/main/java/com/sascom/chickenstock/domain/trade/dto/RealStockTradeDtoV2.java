package com.sascom.chickenstock.domain.trade.dto;

public record RealStockTradeDtoV2 (
        Long companyId,
        Long currentPrice,
        Long transactionVolume,
        TradeType tradeType
) {
}