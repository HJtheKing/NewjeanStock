package com.sascom.chickenstock.domain.trade.service;

import com.sascom.chickenstock.domain.account.dto.response.AccountInfoResponseV2;
import com.sascom.chickenstock.domain.account.dto.response.StockInfoV2;
import com.sascom.chickenstock.domain.history.entity.TradeHistory;
import com.sascom.chickenstock.domain.history.service.HistoryService;
import com.sascom.chickenstock.domain.orderbook.engine.MatchingEngine;
import com.sascom.chickenstock.domain.orderbook.dto.OrderType;
import com.sascom.chickenstock.domain.orderbook.dto.Side;
import com.sascom.chickenstock.domain.orderbook.util.FillEvent;
import com.sascom.chickenstock.domain.orderbook.dto.Order;
import com.sascom.chickenstock.domain.trade.dto.RealStockTradeDtoV2;
import com.sascom.chickenstock.global.events.*;
import com.sascom.chickenstock.global.kafka.kafkaproducer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeServiceV2 {
    private final RestTemplate restTemplate;
    private final KafkaProducer kafkaProducer;
    @Value("${svc.account}")
    private String accountBaseUrl;
    @Value("${svc.portfolio}")
    private String portfolioBaseUrl;
    private final HistoryService historyService;

    private static final long INIT_BALANCE = 50_000_000L;
    private static final int MARGIN_RATE = 10;

    private final MatchingEngine matchingEngine = new MatchingEngine(this::handleFill);
    private final AtomicLong orderIdSeq = new AtomicLong(1L);

    private List<UserStockInfo> userStockInfos;
    private Map<Long, Long> indexMap;
    private final ConcurrentMap<Long, UserState> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, OrderMeta> orderMetaIndex = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Long> preReservedLeft = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Long> orderToAccount = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Long> orderVolumeIndex = new ConcurrentHashMap<>(); // orderId -> 주문 수량(취소 환원용)
    private final ConcurrentMap<Long, Long> borrowedByOrderLeft = new ConcurrentHashMap<>(); // orderId -> 남은 미수 예약액
    private final ConcurrentMap<Long, Long> lastPrices = new ConcurrentHashMap<>();

    public long limitBuy(Long accountId, Long companyId, Long unitPrice, Long volume, boolean margin) {
        UserState st = accounts.computeIfAbsent(accountId, k -> new UserState());
        long orderId = orderIdSeq.getAndIncrement();

        long pre = preReserveCashForBuy(st, accountId, companyId, unitPrice, volume, margin);

        Order o = new Order(orderId, accountId, companyId, Side.BUY, OrderType.LIMIT, unitPrice, volume, System.nanoTime(), margin);
        matchingEngine.submitLimitBuy(o);

        orderMetaIndex.put(orderId, new OrderMeta(companyId, Side.BUY, OrderType.LIMIT, margin, pre));
        preReservedLeft.put(orderId, pre);
        orderToAccount.put(orderId, accountId);
        return orderId;
    }

    public long marketBuy(Long accountId, Long companyId, Long volume, boolean margin) {
        UserState st = accounts.computeIfAbsent(accountId, k -> new UserState());
        long orderId = orderIdSeq.getAndIncrement();

        long refPrice = getLastPrice(companyId);
        long pre = preReserveCashForBuy(st, accountId, companyId, refPrice, volume, margin);

        Order o = new Order(orderId, accountId, companyId, Side.BUY, OrderType.MARKET, null, volume, System.nanoTime(), margin);
        matchingEngine.submitMarketBuy(o);

        orderMetaIndex.put(orderId, new OrderMeta(companyId, Side.BUY, OrderType.MARKET, margin, pre));
        preReservedLeft.put(orderId, pre);
        orderToAccount.put(orderId, accountId);
        return orderId;
    }

    public long limitSell(Long accountId, Long companyId, Long unitPrice, Long volume) {
        UserState st = accounts.computeIfAbsent(accountId, k -> new UserState());
        ensureSufficientHoldings(st, companyId, volume);

        long orderId = orderIdSeq.getAndIncrement();
        st.holdings.computeIfAbsent(companyId, k -> new StockDetails()).sellWaitVolume += volume;

        Order o = new Order(orderId, accountId, companyId, Side.SELL, OrderType.LIMIT, unitPrice, volume, System.nanoTime(), false);
        matchingEngine.submitLimitSell(o);

        orderMetaIndex.put(orderId, new OrderMeta(companyId, Side.SELL, OrderType.LIMIT, false, 0L));
        orderToAccount.put(orderId, accountId);
        return orderId;
    }

    public long marketSell(Long accountId, Long companyId, Long volume) {
        UserState st = accounts.computeIfAbsent(accountId, k -> new UserState());
        ensureSufficientHoldings(st, companyId, volume);

        long orderId = orderIdSeq.getAndIncrement();
        st.holdings.computeIfAbsent(companyId, k -> new StockDetails()).sellWaitVolume += volume;

        Order o = new Order(orderId, accountId, companyId, Side.SELL, OrderType.MARKET, null, volume, System.nanoTime(), false);
        matchingEngine.submitMarketSell(o);

        orderMetaIndex.put(orderId, new OrderMeta(companyId, Side.SELL, OrderType.MARKET, false, 0L));
        orderToAccount.put(orderId, accountId);
        return orderId;
    }

    public boolean cancel(Long accountId, Long orderId) {
        OrderMeta meta = orderMetaIndex.get(orderId);
        if (meta == null) return false;
        boolean ok = matchingEngine.cancel(meta.companyId, orderId);
        if (!ok) return false;

        UserState st = accounts.computeIfAbsent(accountId, k -> new UserState());
        if (meta.side == Side.BUY) {
            long pre = preReservedLeft.getOrDefault(orderId, 0L);
            if (pre > 0) addBalance(st, pre);
            rollbackBorrowedIfAny(st, meta.companyId, orderId);
        } else {
            st.holdings.computeIfAbsent(meta.companyId, k -> new StockDetails()).sellWaitVolume -= metaReservedVolume(orderId);
        }
        cleanupOrderMeta(orderId);
        return true;
    }

    public void processExecution(RealStockTradeDtoV2 dto) {
        matchingEngine.onTick(dto.companyId(), dto.currentPrice(), dto.transactionVolume(), dto.tradeType());
    }

    private void handleFill(FillEvent e) {
        UserState st = accounts.computeIfAbsent(e.accountId, k -> new UserState());
        if (e.side == Side.BUY) {
            settleBorrowedAmountOnFill(st, e.companyId, e.price, e.quantity);
            consumePreReservedOnFill(st, e.orderId, e.companyId, e.price, e.quantity);
            applyLongPositionFill(st, e.companyId, e.price, e.quantity);
        } else {
            applyShortPositionReduce(st, e.companyId, e.quantity);
            addProceedsToBalance(st, e.price, e.quantity);
        }
        if (e.orderCompleted) cleanupOrderMeta(e.orderId);

        // 체결 이력 저장
        try {
            TradeHistory history = TradeHistory.builder()
                    .orderId(e.orderId)
                    .accountId(e.accountId)
                    .companyId(e.companyId)
                    .side(e.side.name())
                    .price(e.price)
                    .quantity(e.quantity)
                    .margin(e.margin)
                    .completed(e.orderCompleted)
                    .build();
            historyService.saveTradeHistory(history);
        } catch (Exception ex) {
            log.error("체결 이력 저장 실패: orderId={}, acc={}, comp={}", e.orderId, e.accountId, e.companyId, ex);
        }

        try {
            // 1) 계좌 정산 요청
            String accountUrl = accountBaseUrl + "/v1/accounts/settle";
            AccountSettleRequest req = new AccountSettleRequest(
                    e.orderId, e.accountId, e.companyId, e.side.name(), e.price, e.quantity, e.margin
            );
            AccountSettleResponse settleRes = restTemplate.postForObject(
                    accountUrl, req, AccountSettleResponse.class
            );
            log.info("Account settled: {}", settleRes);

        } catch (Exception ex) {
            log.error("Orchestration failed for orderId={}", e.orderId, ex);
        }

        try {
            // 2) 포트폴리오 반영 요청
            String portfolioUrl = portfolioBaseUrl + "/v1/positions/" + e.accountId + "/apply-fill";
            PortfolioApplyFillRequest preq = new PortfolioApplyFillRequest(
                    e.orderId, e.accountId, e.companyId, e.side.name(), e.price, e.quantity
            );
            PortfolioApplyFillResponse pres = restTemplate.postForObject(
                    portfolioUrl, preq, PortfolioApplyFillResponse.class
            );
            log.info("Portfolio applied: {}", pres);
        } catch (HttpStatusCodeException | ResourceAccessException ex) {
            log.error("[ACCOUNT] call failed orderId={}, msg={}", e.orderId, ex.getMessage());

            String statusUrl = accountBaseUrl + "/v1/accounts/" + e.accountId + "/settlements/" + e.orderId;
            AccountSettleStatusResponse status = restTemplate.getForObject(statusUrl, AccountSettleStatusResponse.class);
            if (status != null && "APPLIED".equalsIgnoreCase(status.status())) {
                log.warn("[ACCOUNT] POST failed but status=APPLIED. Continue. orderId={}", e.orderId);
            }
        } catch (Exception ex) {
            log.error("Orchestration failed for orderId={}", e.orderId, ex);

            PortfolioCompensationEvent event = new PortfolioCompensationEvent(
                    e.orderId, e.accountId, e.companyId, e.side.name(),
                    "HTTP_FAILED", System.currentTimeMillis()
            );
            kafkaProducer.publishPortfolioCompensation(event);
        }
    }

    private long preReserveCashForBuy(UserState st, Long accountId, Long companyId, Long unitPrice, Long volume, boolean margin) {
        long total = unitPrice * volume;
        if (!margin) {
            deductBalance(st, total);
            return total;
        } else {
            if (st.frozenAccount) throw new IllegalStateException("동결계좌는 미수거래 불가");
            long required = (long) Math.ceil(total / (double) MARGIN_RATE);
            deductBalance(st, required);
            st.borrowedByCompany.merge(companyId, total - required, Long::sum);
            return required;
        }
    }

    private void ensureSufficientHoldings(UserState st, Long companyId, Long volume) {
        StockDetails sd = st.holdings.get(companyId);
        long holdable = (sd == null) ? 0L : (sd.totalVolume - sd.sellWaitVolume);
        if (holdable < volume) throw new IllegalStateException("보유 수량 부족");
    }

    private void settleBorrowedAmountOnFill(UserState st, Long companyId, long price, long qty) {
        long fillAmount = price * qty;
        long borrowed = st.borrowedByCompany.getOrDefault(companyId, 0L);
        if (borrowed <= 0) return;
        long consume = Math.min(borrowed, fillAmount);
        borrowed -= consume;
        st.borrowedByCompany.put(companyId, borrowed);
    }

    private void consumePreReservedOnFill(UserState st, Long orderId, Long companyId, long price, long qty) {
        long fillAmount = price * qty;
        long left = preReservedLeft.getOrDefault(orderId, 0L);
        long use = Math.min(left, fillAmount);
        if (use > 0) preReservedLeft.computeIfPresent(orderId, (k, v) -> v - use);
        long deficit = fillAmount - use;
        if (deficit > 0) {
            long fromCash = Math.min(deficit, st.balance);
            if (fromCash > 0) deductBalance(st, fromCash);
            long remain = deficit - fromCash;
            if (remain > 0) st.borrowedByCompany.merge(companyId, remain, Long::sum);
        }
    }

    private void applyLongPositionFill(UserState st, Long companyId, long price, long qty) {
        StockDetails sd = st.holdings.computeIfAbsent(companyId, k -> new StockDetails());
        sd.totalVolume += qty;
        sd.priceSum += price * qty;
    }

    private void applyShortPositionReduce(UserState st, Long companyId, long qty) {
        StockDetails sd = st.holdings.computeIfAbsent(companyId, k -> new StockDetails());
        long wait = Math.min(sd.sellWaitVolume, qty);
        sd.sellWaitVolume -= wait;
        long remain = qty - wait;
        if (remain > 0) {
            if (sd.totalVolume < remain) throw new IllegalStateException("보유 수량 부족(체결)");
            sd.totalVolume -= remain;
            long preTotal = sd.totalVolume + remain;
            long avg = preTotal == 0 ? 0 : sd.priceSum / preTotal;
            sd.priceSum -= avg * remain;
        }
    }

    private void addProceedsToBalance(UserState st, long price, long qty) {
        addBalance(st, price * qty);
    }

    /* ===================== 잔고/예치/미수 유틸 ===================== */
    private void deductBalance(UserState st, long amount) {
        if (st.balance < amount) throw new IllegalStateException("현금 잔고 부족");
        st.balance -= amount;
    }

    private void addBalance(UserState st, long amount) {
        st.balance += amount;
    }

    private void rollbackBorrowedIfAny(UserState st, Long companyId, Long orderId) {
        // 주문별 미수 예약을 따로 뒀다면 롤백, 여기서는 회사별 총계만 유지
        long left = borrowedByOrderLeft.getOrDefault(orderId, 0L);
        if (left <= 0) return;
        borrowedByOrderLeft.remove(orderId);
        long byCompany = st.borrowedByCompany.getOrDefault(companyId, 0L);
        long newCompany = Math.max(0L, byCompany - left);
        st.borrowedByCompany.put(companyId, newCompany);
    }

    private long metaReservedVolume(Long orderId) {
        return orderVolumeIndex.getOrDefault(orderId, 0L);
    }

    private void cleanupOrderMeta(Long orderId) {
        orderMetaIndex.remove(orderId);
        preReservedLeft.remove(orderId);
        orderToAccount.remove(orderId);
        orderVolumeIndex.remove(orderId);
        borrowedByOrderLeft.remove(orderId);
    }

    private long getLastPrice(Long companyId) {
        Long p = lastPrices.get(companyId);
        if (p == null) throw new IllegalStateException("최근가가 없습니다. 틱 수신 후 시도하세요");
        return p;
    }

    public AccountInfoResponseV2 getAccountInfo(Long accountId) {
        UserStockInfo userStockInfo = userStockInfos.get((int) getUserIndex(accountId));
        List<StockInfoV2> stockInfoList = userStockInfo.holdings
                .entrySet()
                .stream()
                .map(entry -> new StockInfoV2(
                        entry.getKey(),
                        entry.getValue().priceSum,
                        entry.getValue().totalVolume)
                ).toList();
        return new AccountInfoResponseV2(userStockInfo.balance, stockInfoList);
    }
    private long getUserIndex(Long accountId){
        if(!indexMap.containsKey(accountId)){
            indexMap.put(accountId, orderIdSeq.getAndIncrement());
            userStockInfos.add(new UserStockInfo());
        }
        return indexMap.get(accountId);
    }

    private static class UserState {
        long balance = INIT_BALANCE;
        boolean frozenAccount = false;
        Map<Long, StockDetails> holdings = new ConcurrentHashMap<>();
        Map<Long, Long> borrowedByCompany = new ConcurrentHashMap<>();
    }

    private static class StockDetails {
        long totalVolume = 0L;
        long sellWaitVolume = 0L;
        long priceSum = 0L; // 가중평단 계산용
    }

    private record OrderMeta(Long companyId, Side side, OrderType type, boolean margin, long preReservedAmount) {}

    private class UserStockInfo {
        Long balance;
        Map<Long, StockDetails> holdings;
    }
}
