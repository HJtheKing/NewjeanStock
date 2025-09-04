package com.sascom.chickenstock.domain.orderbook.dto;

public class Order {
    public final long orderId;
    public final long accountId;
    public final long companyId; // = symbol
    public final Side side; // BUY / SELL
    public final OrderType type; // LIMIT / MARKET

    // LIMIT에서만 사용, MARKET은 null 가능
    public final Long price;

    // 남은 수량 (체결되면 감소)
    public long remaining;

    // 접수 시각(나노) – 동일 가격대 FIFO
    public final long tsNanos;

    // 증거금(미수) 여부
    public final boolean margin;

    public Order(long orderId,
                 long accountId,
                 long companyId,
                 Side side,
                 OrderType type,
                 Long price,
                 long remaining,
                 long tsNanos,
                 boolean margin) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.companyId = companyId;
        this.side = side;
        this.type = type;
        this.price = price;
        this.remaining = remaining;
        this.tsNanos = tsNanos;
        this.margin = margin;
    }
}