package com.sascom.chickenstock.account.domain.service;
import com.sascom.chickenstock.account.api.dto.PortfolioCompensationEvent; import com.sascom.chickenstock.account.domain.*; import com.sascom.chickenstock.account.infra.repo.*; import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Slf4j @Service @RequiredArgsConstructor public class AccountCompensationService {
  private final AccountRepository accountRepo; private final PreReserveRepository preRepo; private final MarginLedgerRepository marginRepo; private final SettlementRecordRepository recordRepo;
  @Transactional public void compensate(PortfolioCompensationEvent e){ log.warn("Compensate orderId={}, reason={}", e.orderId(), e.reason()); var acc=accountRepo.findById(e.accountId()).orElse(null); var pr=preRepo.findByOrderIdAndAccountId(e.orderId(), e.accountId()).orElse(null); var ml=marginRepo.findByAccountIdAndCompanyId(e.accountId(), e.companyId()).orElse(null);
    if(acc==null) return; if(pr!=null && pr.getReservedLeft()!=null && pr.getReservedLeft()>0){ acc.add(pr.getReservedLeft()); pr.setReservedLeft(0L);} if(pr!=null && pr.getBorrowedLeft()!=null && pr.getBorrowedLeft()>0){ if(ml!=null){ ml.setBorrowedTotal(Math.max(0L, ml.getBorrowedTotal()-pr.getBorrowedLeft())); } pr.setBorrowedLeft(0L);} recordRepo.findByOrderIdAndAccountId(e.orderId(), e.accountId()).ifPresent(r-> r.failed());
    accountRepo.save(acc); if(pr!=null) preRepo.save(pr); if(ml!=null) marginRepo.save(ml);
  }
}
