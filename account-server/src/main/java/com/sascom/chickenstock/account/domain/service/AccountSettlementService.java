package com.sascom.chickenstock.account.domain.service;
import com.sascom.chickenstock.account.api.dto.*; import com.sascom.chickenstock.account.domain.*; import com.sascom.chickenstock.account.infra.repo.*; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor public class AccountSettlementService {
  private final AccountRepository accountRepo; private final PreReserveRepository preRepo; private final MarginLedgerRepository marginRepo; private final SettlementRecordRepository recordRepo;
  @Transactional public AccountSettleResponse settle(AccountSettleRequest req){
    var existed = recordRepo.findByOrderIdAndAccountId(req.orderId(), req.accountId());
    if (existed.isPresent() && existed.get().getStatus()==SettlementStatus.APPLIED){ var r=existed.get(); return new AccountSettleResponse(req.orderId(), r.getSettledAmount(), r.getPreReservedConsumed(), r.getBorrowedConsumed(), r.getBalanceDelta()); }
    else if (existed.isEmpty()) recordRepo.save(SettlementRecord.pending(req.orderId(), req.accountId()));
    var acc = accountRepo.findById(req.accountId()).orElseThrow();
    var pr  = preRepo.findByOrderIdAndAccountId(req.orderId(), req.accountId()).orElseGet(()->PreReserve.of(req.orderId(), req.accountId(), req.companyId(), 0L, 0L));
    var ml  = marginRepo.findByAccountIdAndCompanyId(req.accountId(), req.companyId()).orElseGet(()->marginRepo.save(MarginLedger.of(req.accountId(), req.companyId())));
    long fillAmount=req.price()*req.quantity(); long borrowed=0, pre=0, delta=0;
    if ("BUY".equalsIgnoreCase(req.side())){ borrowed += pr.consumeBorrowed(fillAmount); ml.consumeBorrowed(borrowed); long remain=fillAmount-borrowed; pre += pr.consumeReserved(remain); long remain2=remain-pre; if(remain2>0){ long fromCash=Math.min(acc.getBalance(), remain2); if(fromCash>0){ acc.deduct(fromCash); delta-=fromCash; } long miss=remain2-fromCash; if(miss>0) ml.addBorrowed(miss);} }
    else { acc.add(fillAmount); delta += fillAmount; }
    preRepo.save(pr); marginRepo.save(ml); accountRepo.save(acc);
    var rec = recordRepo.findByOrderIdAndAccountId(req.orderId(), req.accountId()).orElseThrow(); rec.applied(fillAmount, pre, borrowed, delta); recordRepo.save(rec);
    return new AccountSettleResponse(req.orderId(), fillAmount, pre, borrowed, delta);
  }
  @Transactional(readOnly=true) public AccountSettleStatusResponse status(Long orderId, Long accountId){
    return recordRepo.findByOrderIdAndAccountId(orderId, accountId).map(r-> new AccountSettleStatusResponse(r.getStatus().name(), r.getSettledAmount(), r.getPreReservedConsumed(), r.getBorrowedConsumed(), r.getBalanceDelta())).orElse(new AccountSettleStatusResponse("NOT_FOUND", null, null, null, null));
  }
}
