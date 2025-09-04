package com.sascom.chickenstock.domain.orderbook.util;

import com.sascom.chickenstock.domain.orderbook.dto.Side;

/**
 * 체결 이벤트 – 매칭 엔진 -> TradeServiceV2
 */
public class FillEvent {
    public final long orderId;
    public final long accountId;
    public final long companyId;
    public final Side side;
    public final long price;
    public final long quantity;
    public final boolean margin;
    public final boolean orderCompleted; // 해당 주문이 이 체결로 완전히 끝났는지

    public FillEvent(long orderId, long accountId, long companyId, Side side,
                     long price, long quantity, boolean margin, boolean orderCompleted) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.companyId = companyId;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.margin = margin;
        this.orderCompleted = orderCompleted;
    }
}