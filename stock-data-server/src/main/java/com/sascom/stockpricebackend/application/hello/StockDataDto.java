package com.sascom.stockpricebackend.application.hello;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockDataDto {

    private String stockCode;
    private String transactionTime;
    private String currentPrice;
    private String changeSign;
    private String change;
    private String changeRate;
    private String weightedAveragePrice;
    private String openingPrice;
    private String highestPrice;
    private String lowestPrice;
    private String sellOffer1;
    private String buyOffer1;
    private String transactionVolume;
    private String cumulativeVolume;
    private String cumulativeAmount;
    private String sellTransactions;
    private String buyTransactions;
    private String netBuyTransactions;
    private String transactionStrength;
    private String totalSellQuantity;
    private String totalBuyQuantity;
    private String transactionType;
    private String buyRatio;
    private String previousVolumeChangeRate;
    private String openingTime;
    private String openingChangeType;
    private String openingChange;
    private String highestTime;
    private String highestChangeType;
    private String highestChange;
    private String lowestTime;
    private String lowestChangeType;
    private String lowestChange;
    private String businessDate;
    private String operationTypeCode;
    private String tradingHalt;
    private String remainingSellOffer;
    private String remainingBuyOffer;
    private String totalRemainingSellOffer;
    private String totalRemainingBuyOffer;
    private String volumeTurnoverRate;
    private String previousSameTimeVolume;
    private String previousSameTimeVolumeRate;
    private String timeTypeCode;

}
