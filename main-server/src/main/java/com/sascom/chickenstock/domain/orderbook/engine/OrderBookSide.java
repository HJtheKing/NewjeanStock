package com.sascom.chickenstock.domain.orderbook.engine;

import com.sascom.chickenstock.domain.orderbook.dto.Side;
import com.sascom.chickenstock.domain.orderbook.dto.Order;
import com.sascom.chickenstock.domain.orderbook.dto.PriceLevel;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 한쪽 사이드(BID/ASK) 오더 컨테이너
 */
public class OrderBookSide {
    // 가격 레벨: BUY는 높은 가격 우선(내림차순), SELL은 낮은 가격 우선(오름차순)
    public final NavigableMap<Long, PriceLevel> levels;
    // 시장가 대기열
    public final Deque<Order> marketQueue = new ArrayDeque<>();

    public OrderBookSide(Side side) {
        this.levels = (side == Side.BUY) ? new TreeMap<>(Comparator.reverseOrder()) : new TreeMap<>();
    }
}