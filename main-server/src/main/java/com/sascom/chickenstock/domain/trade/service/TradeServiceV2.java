package com.sascom.chickenstock.domain.trade.service;

import com.sascom.chickenstock.domain.account.dto.response.AccountInfoResponseV2;
import com.sascom.chickenstock.domain.account.dto.response.StockInfoV2;
import com.sascom.chickenstock.domain.account.dto.response.UnexecutedStockInfoResponseV2;
import com.sascom.chickenstock.domain.account.dto.response.UnexecutedStockInfoV2;
import com.sascom.chickenstock.domain.history.repository.HistoryRepository;
import com.sascom.chickenstock.domain.history.service.HistoryService;
import com.sascom.chickenstock.domain.trade.dto.OrderType;
import com.sascom.chickenstock.domain.trade.dto.RealStockTradeDtoV2;
import com.sascom.chickenstock.domain.trade.dto.TradeType;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TradeServiceV2 {
    private static final long INIT_BALANCE = 50_000_000L;
    private static final int MARGIN_RATE = 10;

    AtomicInteger counter; // mapping accountId -> counter
    AtomicLong orderIdCounter;
    private Map<Long, Integer> indexMap; // manage accountId and counter
    private List<UserStockInfo> userStockInfos; //
    private Deque<Map<Integer, Long>> userUnpaidAmount;
    // TODO: 현재 대회 순위

    public TradeServiceV2(){
        counter = new AtomicInteger(0);
        orderIdCounter = new AtomicLong(0);
        indexMap = new ConcurrentHashMap<>();
        userStockInfos = new ArrayList<>();
        userUnpaidAmount = new ConcurrentLinkedDeque<>();
    }

    @PostConstruct
    public void init(){
        for(int iteration = 0; iteration < 3; iteration++) {
            userUnpaidAmount.addLast(new ConcurrentHashMap<>());
        }
    }

    @Scheduled(cron = "0 0 18 * * MON-FRI")
    private void processUnpaidAmount() {
        // 미수금 정산
        for(UserStockInfo userStockInfo : userStockInfos) {

        }
        // 현재순위.
    }

    @Scheduled(cron = "0 0 20 * * FRI")
    private void processResult() {
        // 모의주식 투자 처리해도 정산.
        
        // 랭킹 반영
    }

    private void processReverseTrading() {
        // TODO: 반대매매는 어디까지?

        // TODO: 동결계좌 처리

    }

    public long limitBuy(Long accountId, Long companyId, Long unitPrice, Long volume, boolean outstandingTransaction) throws Exception {
        UserStockInfo userStockInfo = userStockInfos.get(getUserIndex(accountId));
        if(outstandingTransaction && userStockInfo.frozenAccount){
            // TODO: Custom error로 변경
            throw new IllegalStateException("동결계좌라 미수 거래 불가");
        }
        long orderId = orderIdCounter.getAndIncrement();
        userStockInfo.orderBuyBook(OrderType.LIMIT, orderId, companyId, unitPrice, volume, outstandingTransaction);
        return orderId;
    }

    public long marketBuy(Long accountId, Long companyId, Long volume, boolean outstandingTransaction) throws Exception {
        UserStockInfo userStockInfo = userStockInfos.get(getUserIndex(accountId));
        if(outstandingTransaction && userStockInfo.frozenAccount){
            // TODO: Custom error로 변경
            throw new IllegalStateException("동결계좌라 미수 거래 불가");
        }
        long orderId = orderIdCounter.getAndIncrement();
        userStockInfo.orderBuyBook(OrderType.MARKET, orderId, companyId, null, volume, outstandingTransaction);
        return orderId;
    }

    public long limitSell(Long accountId, Long companyId, Long unitPrice, Long volume) throws Exception {
        UserStockInfo userStockInfo = userStockInfos.get(getUserIndex(accountId));
        long orderId = orderIdCounter.getAndIncrement();
        userStockInfo.orderSellBook(OrderType.LIMIT, orderId, companyId, unitPrice, volume);
        return orderId;
    }

    public long marketSell(Long accountId, Long companyId, Long volume) throws Exception {
        UserStockInfo userStockInfo = userStockInfos.get(getUserIndex(accountId));
        long orderId = orderIdCounter.getAndIncrement();
        userStockInfo.orderSellBook(OrderType.MARKET, orderId, companyId, null, volume);
        return orderId;
    }

    public void cancel(Long accountId, Long orderId) throws Exception {
        UserStockInfo userStockInfo = userStockInfos.get(getUserIndex(accountId));
        userStockInfo.cancelOrderBook(orderId);
    }

    public void processExecution(RealStockTradeDtoV2 realStockTradeDto) {
        Map<Integer, Long> t2Map = userUnpaidAmount.getLast();
        int totalUser = userUnpaidAmount.size();
        for(int index = 0; index < totalUser; index++) {
            UserStockInfo userStockInfo = userStockInfos.get(index);
            // TODO: execute user order and add to holdings.
            long result = userStockInfo.process(realStockTradeDto);
            if(t2Map.containsKey(index)){
                t2Map.put(index, t2Map.get(index) + result);
            }
            else{
                t2Map.put(index, result);
            }
        }
        // TODO: save executions into historyRepository.
        return;
    }

    public AccountInfoResponseV2 getAccountInfo(Long accountId) {
        UserStockInfo userStockInfo = userStockInfos.get(getUserIndex(accountId));
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

    public UnexecutedStockInfoResponseV2 getBookList(Long accountId) {
        UserStockInfo userStockInfo = userStockInfos.get(getUserIndex(accountId));
        List<UnexecutedStockInfoV2> unexecution = userStockInfo.orderList
                .stream()
                .map(orderBookDetail -> new UnexecutedStockInfoV2(
                        orderBookDetail.companyId,
                        orderBookDetail.unitPrice,
                        orderBookDetail.orderVolume - orderBookDetail.executedVolume,
                        orderBookDetail.orderType,
                        orderBookDetail.tradeType,
                        orderBookDetail.orderId
                ))
                .toList();
        return new UnexecutedStockInfoResponseV2(unexecution);
    }


    private int getUserIndex(Long accountId){
        if(!indexMap.containsKey(accountId)){
            indexMap.put(accountId, counter.getAndIncrement());
            userStockInfos.add(new UserStockInfo());
        }
        return indexMap.get(accountId);
    }

    // 임시로 static으로 만듦. dto로 뺄 예정.
    private static class UserStockInfo {
        Long balance;
        Map<Long, StockDetails> holdings;
        boolean frozenAccount;
        Deque<OrderBookDetails> orderList;

        public UserStockInfo() {
            this.balance = INIT_BALANCE;
            holdings = new ConcurrentHashMap<>();
            frozenAccount = false;
            orderList = new ConcurrentLinkedDeque<>();
        }

        public void orderSellBook(OrderType orderType, Long orderId, Long companyId, Long price, Long orderVolume) {
            StockDetails stockDetails = holdings.get(orderId);
            if(stockDetails == null ||
                    stockDetails.totalVolume - stockDetails.sellWaitVolume < orderVolume){
                // TODO: TradeException으로 바꾸기
                throw new IllegalStateException("보유 개수보다 더 많이 파려고 함");
            }
            orderList.addLast(new OrderBookDetails(TradeType.SELL, orderType, orderId, companyId, price, 0L, 0L, orderVolume, false));
            stockDetails.sellWaitVolume += orderVolume;
            return;
        }

        public void orderBuyBook(OrderType orderType, Long orderId, Long companyId, Long price, Long orderVolume, boolean outstandingTransaction) throws Exception {
            if(outstandingTransaction && frozenAccount){
                // TODO: TradeException.
                throw new IllegalStateException("동결계좌인데 미수거래 하려고 함.");
            }
            // 금액 check
            long totalPrice = price * orderVolume;
            if(outstandingTransaction){
                long requiredBalance = (totalPrice + MARGIN_RATE - 1) / MARGIN_RATE;
                long unpaidAmount = totalPrice - requiredBalance;
                if(balance < requiredBalance){
                    // TODO: TradeException.
                    throw new IllegalStateException("잔고 부족");
                }
                orderList.addLast(
                        new OrderBookDetails(
                                TradeType.BUY, orderType, orderId,
                                companyId, price, requiredBalance, unpaidAmount,
                                orderVolume,
                                true
                        )
                );
                balance -= requiredBalance;
            }
            else{
                if(balance < totalPrice){
                    // TODO: TradeException.
                    throw new IllegalStateException("잔고 부족");
                }
                orderList.addLast(
                        new OrderBookDetails(
                                TradeType.BUY, orderType, orderId,
                                companyId, price, totalPrice, 0L,
                                orderVolume,
                                true
                        )
                );
                balance -= totalPrice;
            }
        }

        public void cancelALlOrderBook(){
            // TODO: 취소 리스트 내보내기. 장 마감후 주문 취소시키기 위해서. 후순위
        }

        public void cancelOrderBook(Long orderId){
            for(OrderBookDetails orderBookDetails : orderList){
                if(!orderBookDetails.orderId.equals(orderId))
                    continue;
                // 즉시 돌려줄 금액
                long result = 0;
                if(TradeType.BUY.equals(orderBookDetails.tradeType)){
                    if(orderBookDetails.accountAmount > 0){
                        result = orderBookDetails.accountAmount;
                    }
                }
                orderList.remove(orderBookDetails);
                balance += result;
                return;
            }
            // TODO: 이미 처리되었다고 Exception
            throw new IllegalStateException("이미 처리되었거나 없는 orderId");
        }

        public long process(RealStockTradeDtoV2 realStockTradeDto){
            Long transactionVolume = realStockTradeDto.transactionVolume();
            Long executedVolume = 0L;
            Long companyId = realStockTradeDto.companyId();
            Long realUnitPrice = realStockTradeDto.currentPrice();
            StockDetails stockDetail = holdings.get(companyId);
            if(stockDetail == null){
                stockDetail = holdings.put(companyId, new StockDetails());
            }
            // result: 이틀 뒤 들어올 금액 or 미수금.
            long result = 0;
            for(Iterator<OrderBookDetails> it = orderList.iterator();
                it.hasNext() && transactionVolume > executedVolume;
                ) {
                OrderBookDetails detail = it.next();
                if (!companyId.equals(detail.companyId) ||
                    detail.tradeType.equals(realStockTradeDto.tradeType())) {
                    continue;
                }
                if(TradeType.SELL.equals(detail.tradeType)){
                    // 지정가인경우 가격이 더 싸면 x
                    if(OrderType.LIMIT.equals(detail.orderType)){
                        if(realUnitPrice < detail.unitPrice) {
                            continue;
                        }
                    }
                    long volume = transactionVolume - executedVolume;
                    volume = Math.min(volume, detail.orderVolume - detail.executedVolume);
                    volume = Math.min(volume, holdings.get(companyId).sellWaitVolume);
                    executedVolume += volume;
                    detail.orderVolume -= volume;
                    result += volume * realUnitPrice;
                    stockDetail.totalVolume -= volume;
                    stockDetail.sellWaitVolume -= volume;
                }
                else{
                    // 지정가인경우 가격이 더 비싸면 x
                    if(OrderType.LIMIT.equals(detail.orderType)){
                        if(realUnitPrice > detail.unitPrice){
                            continue;
                        }
                    }
                    long volume = Math.min(transactionVolume - executedVolume, detail.orderVolume - detail.executedVolume);
                    Long totalAmount = detail.accountAmount + detail.borrowedAmount;
                    if(totalAmount < realUnitPrice){
                        it.remove();
                        continue;
                    }
                    volume = Math.min(volume, totalAmount / realUnitPrice);
                    executedVolume += volume;
                    detail.orderVolume -= volume;
                    detail.accountAmount -= realUnitPrice * volume;
                    if(detail.accountAmount < 0){
                        detail.borrowedAmount -= detail.accountAmount;
                        result += detail.accountAmount;
                        detail.accountAmount = 0L;
                    }
                    stockDetail.totalVolume += volume;
                }
                if(detail.orderVolume.equals(detail.executedVolume)){
                    it.remove();
                }
            }
            // TODO: history 내보내기로 바꾸기, or 체결된 개수 + 금액정도로 바꾸기.
            if(stockDetail.totalVolume == 0){
                holdings.remove(companyId);
            }
            return result;
        }
    }

    private static class StockDetails {
        Long totalVolume;
        Long sellWaitVolume;
        Long priceSum;

        public StockDetails(){
            totalVolume = 0L;
            sellWaitVolume = 0L;
            priceSum = 0L;
        }
    }

    private static class OrderBookDetails {
        TradeType tradeType;
        OrderType orderType;
        Long orderId;
        Long companyId;
        Long unitPrice;
        Long accountAmount;
        Long borrowedAmount;
        Long orderVolume;
        Long executedVolume;
        boolean outstandingTransaction;
        public OrderBookDetails(
                TradeType tradeType,
                OrderType orderType,
                Long orderId,
                Long companyId,
                Long unitPrice,
                Long accountAmount,
                Long borrowedAmount,
                Long orderVolume,
                boolean outstandingTransaction
        ) {
            this.tradeType = tradeType;
            this.orderType = orderType;
            this.orderId = orderId;
            this.companyId = companyId;
            this.unitPrice = unitPrice;
            this.orderVolume = orderVolume;
            this.accountAmount = accountAmount;
            this.borrowedAmount = borrowedAmount;
            this.executedVolume = 0L;
            this.outstandingTransaction = outstandingTransaction;
        }
    }
}
