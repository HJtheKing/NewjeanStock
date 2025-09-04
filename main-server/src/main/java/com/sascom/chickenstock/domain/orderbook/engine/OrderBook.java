package com.sascom.chickenstock.domain.orderbook.engine;

import com.sascom.chickenstock.domain.orderbook.dto.OrderIndex;
import com.sascom.chickenstock.domain.orderbook.dto.OrderType;
import com.sascom.chickenstock.domain.orderbook.dto.Side;
import com.sascom.chickenstock.domain.orderbook.dto.Order;
import com.sascom.chickenstock.domain.orderbook.dto.PriceLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

/**
 * 단일 심볼(companyId) 오더북
 */
public class OrderBook {
    public final long companyId;
    public final OrderBookSide bids; // BUY
    public final OrderBookSide asks; // SELL

    // 주문 인덱스 – 취소 O(1)
    private final Map<Long, OrderIndex> index = new HashMap<>();

    public OrderBook(long companyId) {
        this.companyId = companyId;
        this.bids = new OrderBookSide(Side.BUY);
        this.asks = new OrderBookSide(Side.SELL);
    }

    public void addOrder(Order o) {
        if (o.type == OrderType.MARKET) {
            (o.side == Side.BUY ? bids.marketQueue : asks.marketQueue).addLast(o);
            index.put(o.orderId, OrderIndex.market(o.side));
            return;
        }
        // LIMIT
        NavigableMap<Long, PriceLevel> book = (o.side == Side.BUY) ? bids.levels : asks.levels;
        PriceLevel level = book.computeIfAbsent(o.price, p -> new PriceLevel());
        level.add(o);
        index.put(o.orderId, OrderIndex.limit(o.side, o.price));
    }

    public boolean removeOrder(long orderId) {
        OrderIndex oi = index.remove(orderId);
        if (oi == null) return false;
        if (oi.isMarket) {
            // 시장가 큐에서 선형 검색 제거(잔량 적어 영향 미미). 필요시 보조 인덱스 추가 가능
            return removeFromMarketQueue(oi.side, orderId);
        } else {
            NavigableMap<Long, PriceLevel> book = (oi.side == Side.BUY) ? bids.levels : asks.levels;
            PriceLevel level = book.get(oi.price);
            if (level == null) return false;
            boolean ok = level.removeById(orderId);
            if (level.isEmpty()) book.remove(oi.price);
            return ok;
        }
    }

    private boolean removeFromMarketQueue(Side side, long orderId) {
        var q = (side == Side.BUY) ? bids.marketQueue : asks.marketQueue;
        return q.removeIf(o -> o.orderId == orderId);
    }

    public OrderBookSide side(Side side) { return side == Side.BUY ? bids : asks; }
}