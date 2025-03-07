package com.sascom.chickenstock.domain.trade.dto.request;

import com.sascom.chickenstock.domain.trade.dto.OrderType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class TradeRequest {
    private final OrderType orderType;
    private final Long accountId;
    private final Long memberId;
    private final Long companyId;
    private final Long competitionId;
    private final Long historyId;
    private final Long unitCost;
    private final Long volume;
    private Long executedVolume;
    private final LocalDateTime orderTime;

    public TradeRequest(OrderType orderType,
                        Long accountId, Long memberId, Long companyId, Long competitionId, Long historyId,
                        Long unitCost, Long volume, LocalDateTime orderTime) {
        this.orderType = orderType;
        this.accountId = accountId;
        this.memberId = memberId;
        this.companyId = companyId;
        this.competitionId = competitionId;
        this.historyId = historyId;
        this.unitCost = unitCost;
        this.volume = volume;
        this.executedVolume = 0L;
        this.orderTime = orderTime;
    }

    abstract public int compareByUnitCost(Long cost);

    /**
     * compareByOrderTime - 시간우선의 원칙
     * @param other - TradeRequest that you want to compare orderTime with this.
     * @return 0 if orderTime of this is  equal   to  that of other.
     *         - if orderTime of this is earlier than that of other.
     *         + if orderTime of this is  later  than that of other.
     */
    public final int compareByOrderTime(TradeRequest other) {
        // ?? other.orderTime해도 compile error가 뜨지 않아요... 왜그럴까유?
        return this.orderTime.compareTo(other.getOrderTime());
    }
    /**
     * compareByVolumeTime - 수량우선의 원칙
     * @param other - TradeRequest that you want to compare volume with this.
     * @return 0 if volume of this is  equal   to  that of other.
     *         - if volume of this is greater than that of other.
     *         + if volume of this is   less  than that of other.
     */
    public final int compareByVolume(TradeRequest other) {
        return -this.volume.compareTo(other.getVolume());
    }

    public final int compareByOrderTimeAndVolume(TradeRequest other) {
        if (compareByOrderTime(other) != 0) {
            return compareByOrderTime(other);
        }
        return compareByVolume(other);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof TradeRequest)) {
            return false;
        }
        return this.historyId.equals(((TradeRequest)o).getHistoryId());
    }

    public final long addExecutedVolume(long value) {
        if(value < 0 || value + executedVolume > volume) {
            throw new IllegalArgumentException("invalid parameter value");
        }
        executedVolume += value;
        return executedVolume;
    }

    public final long getRemainingVolume() {
        return volume - executedVolume;
    }
}