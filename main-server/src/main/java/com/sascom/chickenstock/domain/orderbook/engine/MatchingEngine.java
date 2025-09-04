package com.sascom.chickenstock.domain.orderbook.engine;

import com.sascom.chickenstock.domain.orderbook.util.FillEvent;
import com.sascom.chickenstock.domain.orderbook.util.FillHandler;
import com.sascom.chickenstock.domain.orderbook.dto.OrderType;
import com.sascom.chickenstock.domain.orderbook.dto.Side;
import com.sascom.chickenstock.domain.orderbook.dto.Order;
import com.sascom.chickenstock.domain.orderbook.dto.PriceLevel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MatchingEngine {
    private final Map<Long, OrderBook> books = new HashMap<>(); // companyId → OrderBook
    private final FillHandler fillHandler;

    public MatchingEngine(FillHandler fillHandler) {
        this.fillHandler = fillHandler;
    }

    private OrderBook book(long companyId) {
        return books.computeIfAbsent(companyId, OrderBook::new);
    }

    /**
     * 주문 등록
     */
    public long submitLimitBuy(Order o) { ensure(o, Side.BUY, OrderType.LIMIT); book(o.companyId).addOrder(o); return o.orderId; }
    public long submitMarketBuy(Order o) { ensure(o, Side.BUY, OrderType.MARKET); book(o.companyId).addOrder(o); return o.orderId; }
    public long submitLimitSell(Order o) { ensure(o, Side.SELL, OrderType.LIMIT); book(o.companyId).addOrder(o); return o.orderId; }
    public long submitMarketSell(Order o) { ensure(o, Side.SELL, OrderType.MARKET); book(o.companyId).addOrder(o); return o.orderId; }

    private void ensure(Order o, Side side, OrderType type) {
        if (o.side != side || o.type != type) throw new IllegalArgumentException("Side/Type mismatch");
        if (type == OrderType.LIMIT && o.price == null) throw new IllegalArgumentException("LIMIT requires price");
        if (o.remaining <= 0) throw new IllegalArgumentException("remaining must be > 0");
    }

    /**
     * 주문 취소
     */
    public boolean cancel(long companyId, long orderId) {
        OrderBook b = books.get(companyId);
        if (b == null) return false;
        return b.removeOrder(orderId);
    }

    /* ===================== 틱 처리(매칭) ===================== */
    public void onTick(long companyId, long currentPrice, long transactionVolume, /*TradeType*/ Object tradeTypeUnused) {
        OrderBook b = book(companyId);
        // 틱의 거래 가능 수량 만큼 시장가 → 교차 지정가 순서로 집행
        long remainingTickVol = transactionVolume;

        // 1) BUY 시장가 → 반대편 ASK 소진
        remainingTickVol = matchMarketQueue(b, Side.BUY, currentPrice, remainingTickVol);
        if (remainingTickVol == 0) return;

        // 2) SELL 시장가 → 반대편 BID 소진
        remainingTickVol = matchMarketQueue(b, Side.SELL, currentPrice, remainingTickVol);
        if (remainingTickVol == 0) return;

        // 3) 지정가 교차 – 현재가 기준으로 교차된 레벨만 집행
        remainingTickVol = matchCrossedLimits(b, currentPrice, remainingTickVol);
    }

    private long matchMarketQueue(OrderBook b, Side marketSide, long execPrice, long availVol) {
        if (availVol <= 0) return 0;
        var q = (marketSide == Side.BUY) ? b.bids.marketQueue : b.asks.marketQueue;
        // 시장가 주문은 반대 호가를 먹는다
        Side takeFrom = (marketSide == Side.BUY) ? Side.SELL : Side.BUY;
        while (availVol > 0 && !q.isEmpty()) {
            Order mkt = q.peekFirst();
            long traded = executeAgainstBookSide(b, takeFrom, execPrice, availVol, mkt);
            availVol -= traded;
            if (mkt.remaining == 0) q.pollFirst();
            if (traded == 0) break; // 더 집행할 호가가 없으면 중단
        }
        return availVol;
    }

    private long matchCrossedLimits(OrderBook b, long price, long availVol) {
        if (availVol <= 0) return 0;

        // BUY 체결: asks에서 price 이하 레벨만
        while (availVol > 0) {
            var bestAsk = b.asks.levels.firstEntry();
            if (bestAsk == null || bestAsk.getKey() > price) break;
            availVol -= executeLevel(b, bestAsk.getValue(), bestAsk.getKey(), availVol);
            if (bestAsk.getValue().isEmpty()) b.asks.levels.pollFirstEntry();
        }

        // SELL 체결: bids에서 price 이상 레벨만
        while (availVol > 0) {
            var bestBid = b.bids.levels.firstEntry(); // reverseOrder라 first가 최고가
            if (bestBid == null || bestBid.getKey() < price) break;
            availVol -= executeLevel(b, bestBid.getValue(), bestBid.getKey(), availVol);
            if (bestBid.getValue().isEmpty()) b.bids.levels.pollFirstEntry();
        }
        return availVol;
    }

    private long executeLevel(OrderBook b, PriceLevel level, long levelPrice, long availVol) {
        long consumed = 0L;
        // FIFO 순회 – 중간 제거가 일어날 수 있으므로 iterator로 안전하게
        for (Iterator<Order> it = level.iterator(); it.hasNext() && availVol > 0; ) {
            Order maker = it.next();
            long traded = trade(maker, levelPrice, availVol);
            if (maker.remaining == 0) {
                it.remove();
                b.removeOrder(maker.orderId); // 인덱스 정리
            }
            availVol -= traded;
            consumed += traded;
        }
        return consumed;
    }

    private long executeAgainstBookSide(OrderBook b, Side takeFrom, long execPrice, long availVol, Order taker) {
        var levels = b.side(takeFrom).levels;
        while (availVol > 0) {
            var best = (takeFrom == Side.SELL) ? levels.firstEntry() : levels.firstEntry();
            if (best == null) break;
            long levelPrice = best.getKey();
            // 시장가 – 가격 제한 없음
            long consumed = executeLevel(b, best.getValue(), levelPrice, availVol);
            availVol -= consumed;
            if (best.getValue().isEmpty()) levels.pollFirstEntry();
            if (consumed == 0) break;
            // taker.remaining은 trade()에서 감소됨
            if (taker.remaining == 0) break;
        }
        return availVol;
    }

    private long trade(Order maker, long price, long availVol) {
        // taker: 큐 밖(호출자)
        // maker: 레벨 내부에 존재
        long qty = Math.min(availVol, maker.remaining);
        if (qty <= 0) return 0L;

        // maker 감소
        maker.remaining -= qty;

        // 체결 이벤트 – maker 기준으로도 알림이 필요하지만,
        // 서비스 계층에서 orderId로 구분 처리 가능.
        // 여기서는 maker(레벨 내 주문) 기준으로 알림을 발생시킴.
        fillHandler.onFill(new FillEvent(
                maker.orderId,
                maker.accountId,
                maker.companyId,
                maker.side,
                price,
                qty,
                maker.margin,
                maker.remaining == 0
        ));
        return qty;
    }
}