package com.sascom.chickenstock.domain.account.repository;

import com.sascom.chickenstock.domain.account.entity.Account;
import com.sascom.chickenstock.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByMemberId(Long memberId);

    List<Account> findByCompetitionId(Long competitionId);

    Account findTopByMemberOrderByIdDesc(Member member);
}
