package com.sascom.chickenstock.domain.account.controller;

import com.sascom.chickenstock.domain.account.dto.request.*;
import com.sascom.chickenstock.domain.account.dto.response.*;
import com.sascom.chickenstock.domain.account.service.AccountService;
import com.sascom.chickenstock.domain.trade.dto.response.TradeResponse;
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
    @GetMapping("/v2/{accountId}/unexecution")
    public ResponseEntity<UnexecutedStockInfoResponseV2> getUnexecutedStockInfoV2(@PathVariable("accountId") Long accountId) {
        UnexecutedStockInfoResponseV2 response =  accountService.getUnexecutedContentV2(accountId);
        return ResponseEntity.ok().body(response);
    }

    // 지정가 매수
    @PostMapping("/v2/buy/limit")
    public ResponseEntity<TradeResponse> buyLimitStocks(@RequestBody BuyLimitOrderRequest buyLimitOrderRequest) throws Exception{
        TradeResponse response = accountService.buyLimitStocks(buyLimitOrderRequest);
        return ResponseEntity.ok().body(response);
    }

    // 지정가 매도
    @PostMapping("/v2/sell/limit")
    public ResponseEntity<TradeResponse> sellLimitStocks(@RequestBody SellLimitOrderRequest sellLimitOrderRequest) {
        TradeResponse response = accountService.sellLimitStocks(sellLimitOrderRequest);
        return ResponseEntity.ok().body(response);
    }

    // 시장가 매수
    @PostMapping("/v2/buy/market")
    public ResponseEntity<TradeResponse> buyMarketStocks(@RequestBody BuyMarketOrderRequest buyMarketOrderRequest) {
        TradeResponse response = accountService.buyMarketStocks(buyMarketOrderRequest);

        return ResponseEntity.ok().body(response);
    }

    // 시장가 매도
    @PostMapping("/v2/sell/market")
    public ResponseEntity<TradeResponse> sellMarketStocks(@RequestBody SellMarketOrderRequest sellMarketOrderRequest) {
        TradeResponse response = accountService.sellMarketStocks(sellMarketOrderRequest);

        return ResponseEntity.ok().body(response);
    }

    // 주문 취소
    @PostMapping("/v2/cancel")
    public ResponseEntity<TradeResponse> cancelOrderV2(@RequestBody CancelOrderRequestV2 cancelOrderRequest) {
        TradeResponse response = accountService.cancelStockOrderV2(cancelOrderRequest);
        return ResponseEntity.ok().body(response);
    }
}
