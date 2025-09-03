package com.sascom.chickenstock.domain.competition.service;

import com.sascom.chickenstock.domain.account.entity.Account;
import com.sascom.chickenstock.domain.account.repository.AccountRepository;
import com.sascom.chickenstock.domain.competition.dto.request.CompetitionRequest;
import com.sascom.chickenstock.domain.competition.dto.response.CompetitionInfoResponse;
import com.sascom.chickenstock.domain.competition.dto.response.CompetitionHistoryResponse;
import com.sascom.chickenstock.domain.competition.dto.response.CompetitionListResponse;
import com.sascom.chickenstock.domain.competition.entity.Competition;
import com.sascom.chickenstock.domain.competition.error.code.CompetitionErrorCode;
import com.sascom.chickenstock.domain.competition.error.exception.CompetitionCreateException;
import com.sascom.chickenstock.domain.competition.repository.CompetitionRepository;
import com.sascom.chickenstock.domain.ranking.dto.CompetitionResultDto;
import com.sascom.chickenstock.domain.ranking.service.RankingService;
import com.sascom.chickenstock.domain.ranking.util.RatingCalculatorV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class CompetitionService {
    public static final int START_HOUR = 7;
    public static final int END_HOUR = 17;
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final String adminKey;
    private RankingService rankingService;
    private CompetitionRepository competitionRepository;
    private AccountRepository accountRepository;

    @Autowired
    public CompetitionService(@Value("${chicken-stock.admin-key}") String adminKey, CompetitionRepository competitionRepository, AccountRepository accountRepository, RankingService rankingService){
        this.adminKey = adminKey;
        this.competitionRepository = competitionRepository;
        this.accountRepository = accountRepository;
        this.rankingService = rankingService;
    }

    @Transactional
    public void save(CompetitionRequest competitionRequest){
        Competition competition = new Competition(
                competitionRequest.title(),
                competitionRequest.startAt(),
                competitionRequest.endAt()
        );
        competitionRepository.save(competition);
    }

    public List<CompetitionListResponse> findAllCompetitionByMember(Long memberId) {
        List<Account> accountList = accountRepository.findByMemberId(memberId);

        List<CompetitionListResponse> competitionListResponses = new ArrayList<>();
        for (Account account : accountList) {
            Competition competition = account.getCompetition();
            competitionListResponses.add(CompetitionListResponse.builder()
                    .competitionId(competition.getId())
                    .title(competition.getTitle())
                    .startAt(competition.getStartAt())
                    .endAt(competition.getEndAt())
                    .rank(account.getRanking())
                    .ratingChange(account.getRatingChange())
                    .balance(account.getBalance())
                    .accountId(account.getId())
                    .build()
            );
        }

        return competitionListResponses;
    }

    public List<CompetitionHistoryResponse> findAllHistoryByCompetition(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return account.getHistories().stream()
                .map(history ->
                        CompetitionHistoryResponse.builder()
                                .companyName(history.getCompany().getName())
                                .price(history.getPrice())
                                .quantity(history.getVolume())
                                .status(history.getStatus())
                                .createdAt(history.getCreatedAt())
                                .build())
                .toList();
    }

    public CompetitionInfoResponse findActiveCompetition() {
        LocalDateTime now = LocalDateTime.now();
        Optional<Competition> competition = competitionRepository.findByStartAtBeforeAndEndAtAfter(now, now);

        if (competition.isPresent() && isActiveCompetition(competition.get())) {
            Competition activeCompetition = competition.get();
            return new CompetitionInfoResponse(
                    true,
                    activeCompetition.getId(),
                    activeCompetition.getTitle(),
                    activeCompetition.getStartAt(),
                    activeCompetition.getEndAt()
            );
        }

        return CompetitionInfoResponse.createInactiveCompetitionResponse();
    }

    public boolean isActiveCompetition(Competition competition) {
        LocalDateTime now = LocalDateTime.now();
        return competition.getStartAt().isBefore(now) && competition.getEndAt().isAfter(now);
    }

    @Transactional
    public CompetitionInfoResponse addCompetition(String title, String startDateStr, String endDateStr, String adminKey) {

        if (!this.adminKey.equals(adminKey)) {
            throw CompetitionCreateException.of(CompetitionErrorCode.INVALID_KEY);
        }

        LocalDateTime startDate, endDate;
        try {
            startDate = LocalDate.parse(startDateStr, DATE_TIME_FORMATTER).atStartOfDay().plusHours(START_HOUR);
            endDate = LocalDate.parse(endDateStr, DATE_TIME_FORMATTER).atStartOfDay().plusHours(END_HOUR);
        } catch (DateTimeParseException e) {
            throw CompetitionCreateException.of(CompetitionErrorCode.INVALID_DATE_TYPE);
        }

        if (startDate.isAfter(endDate) || startDate.isBefore(LocalDateTime.now())) {
            throw CompetitionCreateException.of(CompetitionErrorCode.INVALID_DURATION);
        }

        Optional<Competition> candiCompetition = competitionRepository.findTopByOrderByIdDesc();
        if (candiCompetition.isPresent()) {
            Competition latestCompetition = candiCompetition.get();
            LocalDateTime latestEndDate = latestCompetition.getEndAt();

            if (startDate.isBefore(latestEndDate)) {
                throw CompetitionCreateException.of(CompetitionErrorCode.CONFLICT);
            }
        }

        Competition competition = competitionRepository.save(new Competition(title, startDate, endDate));
        return new CompetitionInfoResponse(
                false,
                competition.getId(),
                competition.getTitle(),
                competition.getStartAt(),
                competition.getEndAt()
        );
    }

    @Transactional
    public void finalizeCompetition(Long competitionId) {
        // 1) 이번 대회 참가자 계정 로드
        List<Account> accounts = accountRepository.findByCompetitionId(competitionId);
        if (accounts.isEmpty()) return;

        // 2) 참가자별 "대회 전 누적 레이팅" 준비
        Map<Account, Integer> beforeMap = new HashMap<>();
        for (Account a : accounts) {
            Long memberId = a.getMember().getId();
            int before = rankingService.getRankingById(memberId).getRating();
            // 첫 참가자는 캐시에서 rating=0일 수 있으니 V2가 내부에서 INITIAL로 보정
            beforeMap.put(a, before);
        }

        // 3) 등수 계산 (엔티티에 updateRankingAndRatingChange 반영됨)
        RatingCalculatorV2 calculator = new RatingCalculatorV2();
        List<Account> updated = calculator.processCompetitionResult(beforeMap);

        // 4) 랭킹판 반영
        List<CompetitionResultDto> results = updated.stream()
                .map(a -> new CompetitionResultDto(
                        a.getMember().getId(),
                        a.getRatingChange()
                ))
                .toList();
        rankingService.updateRankingBoardByCompetitionResult(results);
    }

}
