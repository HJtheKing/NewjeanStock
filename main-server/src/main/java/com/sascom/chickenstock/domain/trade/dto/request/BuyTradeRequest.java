package com.sascom.chickenstock.domain.trade.dto.request;

import com.sascom.chickenstock.domain.trade.dto.OrderType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class BuyTradeRequest extends TradeRequest implements Comparable<BuyTradeRequest> {

    @Builder
    public BuyTradeRequest(OrderType orderType,
                           Long accountId, Long memberId, Long companyId, Long competitionId, Long historyId,
                           Long unitCost, Long volume,
                           LocalDateTime orderTime) {
        super(orderType,
                accountId, memberId, companyId, competitionId, historyId,
                unitCost, volume, orderTime);
    }

    @Override
    public int compareByUnitCost(Long cost) {
        return this.getUnitCost().compareTo(cost);
    }

    public int compareByUnitCost(BuyTradeRequest other) {
        if(getOrderType() != other.getOrderType()) {
            return 0;
        }
        return this.compareByUnitCost(other.getUnitCost());
    }

    @Override
    public int compareTo(BuyTradeRequest other) {
        if (compareByUnitCost(other) != 0) {
            return compareByUnitCost(other);
        }
        if (compareByOrderTimeAndVolume(other) != 0) {
            return compareByOrderTimeAndVolume(other);
        }
        return getHistoryId().compareTo(other.getHistoryId());
    }
}
