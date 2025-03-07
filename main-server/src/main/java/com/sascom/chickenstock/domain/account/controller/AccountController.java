package com.sascom.chickenstock.domain.account.controller;

import com.sascom.chickenstock.domain.account.dto.request.*;
import com.sascom.chickenstock.domain.account.dto.response.*;
import com.sascom.chickenstock.domain.account.service.AccountService;
import com.sascom.chickenstock.domain.trade.dto.response.CancelOrderResponse;
import com.sascom.chickenstock.domain.trade.dto.response.TradeResponse;
//import com.sascom.chickenstock.domain.trade.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    private AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public Long createAccount(@RequestBody AccountCreateRequest request) {
        Long accountId = accountService.createAccount(request.memberId(), request.competitionId());
        return accountId;
    }

    // 계좌 정보 조회
    // TradeService에서 조회
//    @GetMapping("/{accountId}")
//    public AccountInfoResponse getAccountInfo(@PathVariable("accountId") Long accountId){
//        return accountService.getAccountInfo(accountId);
//    }

    @GetMapping("/v2/{accountId}")
    public ResponseEntity<AccountInfoResponseV2> getAccountInfoV2(@PathVariable("accountId") Long accountId) {
        AccountInfoResponseV2 response = accountService.getAccountInfoV2(accountId);
        return ResponseEntity.ok().body(response);
    }

    // 체결 정보 조회
    @GetMapping("/{accountId}/execution")
    public ExecutionContentResponse getExecutionContent(@PathVariable("accountId") Long accountId){
        return accountService.getExecutionContent(accountId);
    }

    // 미체결 정보 조회
//    @GetMapping("/{accountId}/unexecution")
//    public UnexecutionContentResponse getUnexecutionContent(@PathVariable("accountId") Long accountId){
//        return accountService.getUnexecutionContent(accountId);
//    }

    @GetMapping("/v2/{accountId}/unexecution")
    public ResponseEntity<UnexecutedStockInfoResponseV2> getUnexecutedStockInfoV2(@PathVariable("accountId") Long accountId) {
        UnexecutedStockInfoResponseV2 response =  accountService.getUnexecutedContentV2(accountId);
        return ResponseEntity.ok().body(response);
    }

//    // 지정가 매수(이전 메소드)
//    @PostMapping("/buy/limit")
//    public ResponseEntity<TradeResponse> buyLimitStocks(@RequestBody StockOrderRequest stockOrderRequest) {
//        TradeResponse response = accountService.buyLimitStocks(stockOrderRequest);
//
//        return ResponseEntity.ok().body(response);
//    }

    // 지정가 매수(신규 메소드)
    @PostMapping("/v2/buy/limit")
    public ResponseEntity<TradeResponse> buyLimitStocks(@RequestBody BuyLimitOrderRequest buyLimitOrderRequest) throws Exception{
        TradeResponse response = accountService.buyLimitStocks(buyLimitOrderRequest);
        return ResponseEntity.ok().body(response);
    }

//    // 지정가 매도(이전 메소드)
//    @PostMapping("/sell/limit")
//    public ResponseEntity<TradeResponse> sellLimitStocks(@RequestBody StockOrderRequest stockOrderRequest {
//        TradeResponse response = accountService.sellLimitStocks(stockOrderRequest);
//
//        return ResponseEntity.ok().body(response);
//    }

    // 지정가 매도(신규 메소드)
    @PostMapping("/v2/sell/limit")
    public ResponseEntity<TradeResponse> sellLimitStocks(@RequestBody SellLimitOrderRequest sellLimitOrderRequest) {
        TradeResponse response = accountService.sellLimitStocks(sellLimitOrderRequest);
        return ResponseEntity.ok().body(response);
    }

//    // 시장가 매수(이전 메소드)
//    @PostMapping("/buy/market")
//    public ResponseEntity<TradeResponse> buyMarketStocks(@RequestBody StockOrderRequest stockOrderRequest) {
//        TradeResponse response = accountService.buyMarketStocks(stockOrderRequest);
//
//        return ResponseEntity.ok().body(response);
//    }

    // 시장가 매수(신규 메소드)
    @PostMapping("/v2/buy/market")
    public ResponseEntity<TradeResponse> buyMarketStocks(@RequestBody BuyMarketOrderRequest buyMarketOrderRequest) {
        TradeResponse response = accountService.buyMarketStocks(buyMarketOrderRequest);

        return ResponseEntity.ok().body(response);
    }

//    // 시장가 매도(이전 메소드)
//    @PostMapping("/sell/market")
//    public ResponseEntity<TradeResponse> sellMarketStocks(@RequestBody StockOrderRequest stockOrderRequest) {
//        TradeResponse response = accountService.sellMarketStocks(stockOrderRequest);
//
//        return ResponseEntity.ok().body(response);
//    }

    // 시장가 매도(신규 메소드)
    @PostMapping("/v2/sell/market")
    public ResponseEntity<TradeResponse> sellMarketStocks(@RequestBody SellMarketOrderRequest sellMarketOrderRequest) {
        TradeResponse response = accountService.sellMarketStocks(sellMarketOrderRequest);

        return ResponseEntity.ok().body(response);
    }

    // 주문 취소
    @PostMapping("/cancel")
    public ResponseEntity<CancelOrderResponse> cancelOrder(@RequestBody CancelOrderRequest cancelOrderRequest) {
//        CancelOrderResponse response = accountService.cancel(cancelOrderRequest);

//        return ResponseEntity.ok().body(response);
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/v2/cancel")
    public ResponseEntity<TradeResponse> cancelOrderV2(@RequestBody CancelOrderRequestV2 cancelOrderRequest) {
        TradeResponse response = accountService.cancelStockOrderV2(cancelOrderRequest);
        return ResponseEntity.ok().body(response);
    }
}
