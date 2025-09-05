package com.sascom.chickenstock.account.api;
import com.sascom.chickenstock.account.api.dto.*; import com.sascom.chickenstock.account.domain.service.AccountSettlementService; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/v1/accounts") @RequiredArgsConstructor public class AccountController {
  private final AccountSettlementService svc;
  @PostMapping("/{accountId}/settle") public AccountSettleResponse settle(@PathVariable Long accountId, @RequestBody AccountSettleRequest req){ return svc.settle(req); }
  @GetMapping("/{accountId}/settlements/{orderId}") public AccountSettleStatusResponse status(@PathVariable Long accountId, @PathVariable Long orderId){ return svc.status(orderId, accountId); }
}
