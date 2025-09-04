package com.sascom.chickenstock.domain.orderbook.dto;

/**
 * 주문 인덱스 – 주문이 어느 컨테이너에 있는지 빠르게 찾기 위한 메타
 */
public class OrderIndex {
    public final Side side;
    public final boolean isMarket;
    public final Long price; // LIMIT일 때만 의미 있음

    private OrderIndex(Side side, boolean isMarket, Long price) {
        this.side = side;
        this.isMarket = isMarket;
        this.price = price;
    }

    public static OrderIndex market(Side side) { return new OrderIndex(side, true, null); }
    public static OrderIndex limit(Side side, long price) { return new OrderIndex(side, false, price); }
}