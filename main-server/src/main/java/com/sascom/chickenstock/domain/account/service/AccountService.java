package com.sascom.chickenstock.domain.account.service;

import com.sascom.chickenstock.domain.account.dto.request.*;
import com.sascom.chickenstock.domain.account.dto.response.*;
import com.sascom.chickenstock.domain.account.entity.Account;
import com.sascom.chickenstock.domain.account.entity.TradeStatus;
import com.sascom.chickenstock.domain.account.error.code.AccountErrorCode;
import com.sascom.chickenstock.domain.account.error.exception.AccountNotEnoughException;
import com.sascom.chickenstock.domain.account.error.exception.AccountNotFoundException;
import com.sascom.chickenstock.domain.account.repository.AccountRepository;
import com.sascom.chickenstock.domain.auth.dto.response.AccountInfoForLogin;
import com.sascom.chickenstock.domain.company.entity.Company;
import com.sascom.chickenstock.domain.company.error.code.CompanyErrorCode;
import com.sascom.chickenstock.domain.company.error.exception.CompanyNotFoundException;
import com.sascom.chickenstock.domain.company.repository.CompanyRepository;
import com.sascom.chickenstock.domain.company.service.CompanyService;
import com.sascom.chickenstock.domain.competition.entity.Competition;
import com.sascom.chickenstock.domain.competition.error.code.CompetitionErrorCode;
import com.sascom.chickenstock.domain.competition.error.exception.CompetitionNotFoundException;
import com.sascom.chickenstock.domain.competition.repository.CompetitionRepository;
import com.sascom.chickenstock.domain.competition.service.CompetitionService;
import com.sascom.chickenstock.domain.history.entity.History;
import com.sascom.chickenstock.domain.history.entity.HistoryStatus;
import com.sascom.chickenstock.domain.history.error.code.HistoryErrorCode;
import com.sascom.chickenstock.domain.history.error.exception.HistoryNotFoundException;
import com.sascom.chickenstock.domain.history.repository.HistoryRepository;
import com.sascom.chickenstock.domain.member.entity.Member;
import com.sascom.chickenstock.domain.member.error.code.MemberErrorCode;
import com.sascom.chickenstock.domain.member.error.exception.MemberNotFoundException;
import com.sascom.chickenstock.domain.member.repository.MemberRepository;
import com.sascom.chickenstock.domain.trade.dto.OrderType;
import com.sascom.chickenstock.domain.trade.dto.TradeType;
import com.sascom.chickenstock.domain.trade.dto.request.BuyTradeRequest;
import com.sascom.chickenstock.domain.trade.dto.request.SellTradeRequest;
import com.sascom.chickenstock.domain.trade.dto.response.CancelOrderResponse;
import com.sascom.chickenstock.domain.trade.dto.response.TradeResponse;
//import com.sascom.chickenstock.domain.trade.service.TradeService;
import com.sascom.chickenstock.domain.trade.service.TradeServiceV2;
import com.sascom.chickenstock.global.util.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final CompetitionRepository competitionRepository;
    private final RedisService redisService;
    //private final TradeService tradeService;
    private final TradeServiceV2 tradeService;
    private final CompetitionService competitionService;
    private final CompanyService companyService;
    private final CompanyRepository companyRepository;

    // 계좌 생성
    @Transactional
    public Long createAccount(Long memberId, Long competitionId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> MemberNotFoundException.of(MemberErrorCode.NOT_FOUND));
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> CompetitionNotFoundException.of(CompetitionErrorCode.NOT_FOUND));
        Account account = new Account(
                member,
                competition
        );
        return accountRepository.save(account).getId();
    }

    // 계좌 정보 조회 (기존 방식)
//    public AccountInfoResponse getAccountInfo(Long accountId) {
//        // 계좌 repo에서 계좌id로 계좌 객체 끌고오기
//        Account account = accountRepository.findById(accountId).
//                orElseThrow(EntityNotFoundException::new);
//
//        // 주식들 정보 담긴 리스트 객체
//        List<StockInfo> stocks = new ArrayList<>();
//        // 사용자 주식 정보 조회
//        Map<String, Map<String, String>> allStockInfo = redisService.getStockInfo(accountId);
//        for (Map.Entry<String, Map<String, String>> entry : allStockInfo.entrySet()) {
//            String key = entry.getKey();
//            Map<String, String> stockData = entry.getValue();
//            StringTokenizer st = new StringTokenizer(key, ":");
//            st.nextToken();
//            st.nextToken();
//            st.nextToken();
//            Long companyId = Long.valueOf(st.nextToken());
//            Company company = companyService.findById(companyId);
//
//            // 새로운 주식 정보 추기?
//            stocks.add(new StockInfo(company.getName(),
//                    Integer.valueOf(stockData.get("price")),
//                    Integer.valueOf(stockData.get("volume")))
//            );
//        }
//
//        // 사용자 계좌 잔고 조회
//        AccountInfoResponse accountInfoResponse = new AccountInfoResponse(
//                account.getBalance(),
//                stocks
//        );
//
//        // 임시로 주석
////        AccountInfoResponse response = tradeService.getAccountInfo(accountId);
//
//        return accountInfoResponse;
//    }

    public AccountInfoResponseV2 getAccountInfoV2(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> AccountNotFoundException.of(AccountErrorCode.NOT_FOUND));
        if(!account.getMember().getId().equals(SecurityUtil.getCurrentMemberId())) {
            AccountNotFoundException.of(AccountErrorCode.INVALID_VALUE);
        }
        return tradeService.getAccountInfo(accountId);
    }

    // 주문 체결 내역 조회
    public ExecutionContentResponse getExecutionContent(Long accountId){
        List<History> histories = historyRepository.findExecutionContent(accountId);
        List<HistoryInfo> result = histories.stream()
                .map(h -> new HistoryInfo(h.getCompany().getName(),
                        h.getPrice(),
                        h.getVolume(),
                        h.getStatus(),
                        h.getCreatedAt()
                ))
                .collect(Collectors.toList());
        return new ExecutionContentResponse(result);
    }


    // 지정가 매수(신규 메소드)
    @Transactional
    public TradeResponse buyLimitStocks(BuyLimitOrderRequest buyLimitOrderRequest) throws Exception {
        // 유효성 검증
        validateStockOrderRequest(buyLimitOrderRequest);

        // 회사 검증
        Company company = companyRepository.findById(buyLimitOrderRequest.companyId())
                .orElseThrow(() -> CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));

        // 계좌 검증
        Account account = accountRepository.findById(buyLimitOrderRequest.accountId())
                .orElseThrow(() -> AccountNotFoundException.of(AccountErrorCode.NOT_FOUND));
        if(!account.getCompetition().getId().equals(buyLimitOrderRequest.competitionId())) {
            throw CompetitionNotFoundException.of(CompetitionErrorCode.NOT_FOUND);
        }

        TradeStatus tradeStatus;
        // 구매 요청
        try {
            Long counter = tradeService.limitBuy(account.getId(), company.getId(), buyLimitOrderRequest.unitCost(), buyLimitOrderRequest.volume(), buyLimitOrderRequest.margin());
            log.info("###############counter: {}", counter);
            tradeStatus = TradeStatus.SUCCESS;
            History history = new History(account, company, buyLimitOrderRequest.unitCost(),
                                          buyLimitOrderRequest.volume(), HistoryStatus.지정가매수요청,
                                          counter);
            historyRepository.save(history);
        }catch (Exception e){
            tradeStatus = TradeStatus.FAILURE;
            log.error("An error occurred for this -> ", e);
        }
        return new TradeResponse(tradeStatus);
    }

    // 지정가 매도(신규 메소드)
    @Transactional
    public TradeResponse sellLimitStocks(SellLimitOrderRequest sellLimitOrderRequest) {
        validateStockOrderRequest(sellLimitOrderRequest);

        Account account = accountRepository.findById(sellLimitOrderRequest.accountId())
                .orElseThrow(() -> AccountNotFoundException.of(AccountErrorCode.NOT_FOUND));
        Company company = companyRepository.findById(sellLimitOrderRequest.companyId())
                .orElseThrow(() -> CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));

        TradeStatus tradeStatus;
        // 구매요청
        try {
            Long counter = tradeService.limitSell(account.getId(), company.getId(), sellLimitOrderRequest.unitCost(), sellLimitOrderRequest.volume());
            tradeStatus = TradeStatus.SUCCESS;
            History history = new History(account, company, sellLimitOrderRequest.unitCost(),
                                          sellLimitOrderRequest.volume(), HistoryStatus.지정가매도요청,
                                          counter);
            historyRepository.save(history);
        }catch (Exception e){
            tradeStatus = TradeStatus.FAILURE;
        }
        return new TradeResponse(tradeStatus);
    }

    // 시장가 매수(신규 메소드)
    @Transactional
    public TradeResponse buyMarketStocks(BuyMarketOrderRequest buyMarketOrderRequest) {
        validateStockOrderRequest(buyMarketOrderRequest);

        Account account = accountRepository.findById(buyMarketOrderRequest.accountId())
                .orElseThrow(() -> AccountNotFoundException.of(AccountErrorCode.NOT_FOUND));
        Company company = companyRepository.findById(buyMarketOrderRequest.companyId())
                .orElseThrow(() -> CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));

        TradeStatus tradeStatus;
        // 구매요청
        try {
            Long counter = tradeService.marketBuy(account.getId(), company.getId(), buyMarketOrderRequest.volume(), buyMarketOrderRequest.margin());
            tradeStatus = TradeStatus.SUCCESS;
            History history = new History(account, company, buyMarketOrderRequest.unitCost(), buyMarketOrderRequest.volume(),
                                          HistoryStatus.시장가매수요청, counter);
            historyRepository.save(history);
        }catch (Exception e){
            tradeStatus = TradeStatus.FAILURE;
        }
        return new TradeResponse(tradeStatus);
    }

    // 시장가 매도(신규 메소드)
    @Transactional
    public TradeResponse sellMarketStocks(SellMarketOrderRequest sellMarketOrderRequest) {
        validateStockOrderRequest(sellMarketOrderRequest);

        Account account = accountRepository.findById(sellMarketOrderRequest.accountId())
                .orElseThrow(() -> AccountNotFoundException.of(AccountErrorCode.NOT_FOUND));
        Company company = companyRepository.findById(sellMarketOrderRequest.companyId())
                .orElseThrow(() -> CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));

        TradeStatus tradeStatus;
        // 구매요청
        try {
            long orderId = tradeService.marketSell(account.getId(), company.getId(), sellMarketOrderRequest.volume());
            tradeStatus = TradeStatus.SUCCESS;
            History history = new History(account, company, sellMarketOrderRequest.unitCost(), sellMarketOrderRequest.volume(),
                    HistoryStatus.시장가매도요청, orderId);
            historyRepository.save(history);
        }catch (Exception e){
            tradeStatus = TradeStatus.FAILURE;
        }
        return new TradeResponse(tradeStatus);
    }

//    // 결제 취소
//    @Transactional
//    public CancelOrderResponse cancelStockOrder(CancelOrderRequest cancelOrderRequest) {
//        // validate member
//        Member member = validateMember(cancelOrderRequest.memberId());
//
//        // validate account
//        Account account = validateAccount(member, cancelOrderRequest.accountId(), cancelOrderRequest.competitionId());
//
//        // validate history
//        History history = validateHistory(account, cancelOrderRequest.historyId());
//
//        CancelOrderResponse response = null;
//        if(HistoryStatus.지정가매도요청.equals(history.getStatus()) ||
//                HistoryStatus.시장가매도요청.equals(history.getStatus())) {
//            SellTradeRequest sellTradeRequest = SellTradeRequest.builder()
//                    .orderType(HistoryStatus.지정가매도요청.equals(history.getStatus())?
//                            OrderType.LIMIT :
//                            OrderType.MARKET)
//                    .accountId(cancelOrderRequest.accountId())
//                    .memberId(cancelOrderRequest.memberId())
//                    .companyId(history.getCompany().getId())
//                    .competitionId(account.getCompetition().getId())
//                    .historyId(history.getId())
//                    .unitCost(history.getPrice())
//                    .totalOrderVolume(history.getVolume())
//                    .orderTime(history.getCreatedAt())
//                    .build();
//            response = tradeService.cancelOrderRequest(sellTradeRequest);
//        }
//        if(HistoryStatus.지정가매수요청.equals(history.getStatus()) ||
//                HistoryStatus.시장가매수요청.equals(history.getStatus())) {
//            BuyTradeRequest buyTradeRequest = BuyTradeRequest.builder()
//                    .orderType(HistoryStatus.지정가매수요청.equals(history.getStatus())?
//                            OrderType.LIMIT :
//                            OrderType.MARKET)
//                    .accountId(cancelOrderRequest.accountId())
//                    .memberId(cancelOrderRequest.memberId())
//                    .companyId(history.getCompany().getId())
//                    .competitionId(account.getCompetition().getId())
//                    .historyId(history.getId())
//                    .unitCost(history.getPrice())
//                    .totalOrderVolume(history.getVolume())
//                    .orderTime(history.getCreatedAt())
//                    .build();
//            response = tradeService.cancelOrderRequest(buyTradeRequest);
//        }
//        if(response == null) {
//            throw new IllegalStateException("server error");
//        }
//        return response;
//    }

    // 미체결 내역 조회
//    public UnexecutionContentResponse getUnexecutionContent(Long accountId) {
//        Map<String,Map<String,String>> Unexcuted = redisService.getUnexcutionContent(accountId);
//        List<UnexcutedStockInfo> unexcutedStockInfos = new ArrayList<>();
//
//        for (Map.Entry<String, Map<String, String>> entry : Unexcuted.entrySet()) {
//            Map<String, String> stockData = entry.getValue();
//
//            unexcutedStockInfos.add(new UnexcutedStockInfo(Long.valueOf("companyId"),
//                    Integer.valueOf(stockData.get("price")),
//                    Integer.valueOf(stockData.get("volume")),
//                    TradeType.valueOf(stockData.get("tradeType")))
//            );
//        }
//        return new UnexecutionContentResponse(unexcutedStockInfos);
//    }

    public UnexecutedStockInfoResponseV2 getUnexecutedContentV2(Long accountId) {
        return tradeService.getBookList(accountId);
    }


    @Transactional
    public TradeResponse cancelStockOrderV2(CancelOrderRequestV2 cancelOrderRequest) {
        Member member = validateMember(cancelOrderRequest.memberId());
        Account account = validateAccount(member, cancelOrderRequest.accountId(), cancelOrderRequest.competitionId());
        try {
            tradeService.cancel(cancelOrderRequest.accountId(), cancelOrderRequest.orderId());
        } catch(Exception e){
            return TradeResponse.builder()
                    .status(TradeStatus.FAILURE)
                    .build();
        }
        return new TradeResponse(TradeStatus.SUCCESS);
    }

    // 유효성 체크 메소드(지정가 매수)
    public void validateStockOrderRequest(BuyLimitOrderRequest buyLimitOrderRequest) {
        // Member 유효성 체크
        Member member = validateMember(buyLimitOrderRequest.memberId());

        // Account 유효성 체크
        Account account = validateAccount(member, buyLimitOrderRequest.accountId(), buyLimitOrderRequest.competitionId());

        // Company 유효성 체크
        Company company = companyRepository.findById(buyLimitOrderRequest.companyId())
                .orElseThrow(() -> CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));

        // Competition 유효성 체크
        Competition competition = competitionRepository.findById(buyLimitOrderRequest.competitionId())
                .orElseThrow(() -> CompetitionNotFoundException.of(CompetitionErrorCode.NOT_FOUND));

        if(!account.getCompetition().getId().equals(buyLimitOrderRequest.competitionId())) {
            throw AccountNotFoundException.of(AccountErrorCode.INVALID_VALUE);
        }
    }

    // 유효성 체크 메소드(지정가 매도)
    public void validateStockOrderRequest(SellLimitOrderRequest sellLimitOrderRequest) {
        // Member 유효성 체크
        Member member = validateMember(sellLimitOrderRequest.memberId());

        // Account 유효성 체크
        Account account = validateAccount(member, sellLimitOrderRequest.accountId(), sellLimitOrderRequest.competitionId());

        // Company 유효성 체크
        Company company = companyRepository.findById(sellLimitOrderRequest.companyId())
                .orElseThrow(() -> CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));

        // Competition 유효성 체크
        Competition competition = competitionRepository.findById(sellLimitOrderRequest.competitionId())
                .orElseThrow(() -> CompetitionNotFoundException.of(CompetitionErrorCode.NOT_FOUND));

        if(!account.getCompetition().getId().equals(sellLimitOrderRequest.competitionId())) {
            throw AccountNotFoundException.of(AccountErrorCode.INVALID_VALUE);
        }
    }

    // 유효성 체크 메소드(시장가 매수)
    public void validateStockOrderRequest(BuyMarketOrderRequest buyMarketOrderRequest) {
        // Member 유효성 체크
        Member member = validateMember(buyMarketOrderRequest.memberId());

        // Account 유효성 체크
        Account account = validateAccount(member, buyMarketOrderRequest.accountId(), buyMarketOrderRequest.competitionId());

        // Company 유효성 체크
        Company company = companyRepository.findById(buyMarketOrderRequest.companyId())
                .orElseThrow(() -> CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));

        // Competition 유효성 체크
        Competition competition = competitionRepository.findById(buyMarketOrderRequest.competitionId())
                .orElseThrow(() -> CompetitionNotFoundException.of(CompetitionErrorCode.NOT_FOUND));

        if(!account.getCompetition().getId().equals(buyMarketOrderRequest.competitionId())) {
            throw AccountNotFoundException.of(AccountErrorCode.INVALID_VALUE);
        }
    }

    // 유효성 체크 메소드(시장가 매도)
    public void validateStockOrderRequest(SellMarketOrderRequest sellMarketOrderRequest) {
        // Member 유효성 체크
        Member member = validateMember(sellMarketOrderRequest.memberId());

        // Account 유효성 체크
        Account account = validateAccount(member, sellMarketOrderRequest.accountId(), sellMarketOrderRequest.competitionId());

        // Company 유효성 체크
        Company company = companyRepository.findById(sellMarketOrderRequest.companyId())
                .orElseThrow(() -> CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));

        // Competition 유효성 체크
        Competition competition = competitionRepository.findById(sellMarketOrderRequest.competitionId())
                .orElseThrow(() -> CompetitionNotFoundException.of(CompetitionErrorCode.NOT_FOUND));

        if(!account.getCompetition().getId().equals(sellMarketOrderRequest.competitionId())) {
            throw AccountNotFoundException.of(AccountErrorCode.INVALID_VALUE);
        }
    }


    // 로그인 정보 조회
    public AccountInfoForLogin getInfoForLogin(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> MemberNotFoundException.of(MemberErrorCode.NOT_FOUND));
        Account account = accountRepository.findTopByMemberOrderByIdDesc(member);

        if (account != null) {
            // 최신 계좌 조회해서 거기에 있는 CompetitonId가 현재의 대회 pk랑 같은지 체크
            Competition accountCompetition = account.getCompetition();
            Optional<Competition> competition = competitionRepository.findById(accountCompetition.getId());

            if (competition.isPresent() && competitionService.isActiveCompetition(competition.get())) { // 지금 열리고 있는 대회
                return AccountInfoForLogin.create(true, account.getId(), account.getBalance(), account.getRanking());
            }
        }

        return AccountInfoForLogin.create(false, null,0L, 0);
    }

    // 사용자 유효성 검증
    private Member validateMember(Long memberId) {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        if(!loginMemberId.equals(memberId)) {
            throw MemberNotFoundException.of(MemberErrorCode.INVALID_VALUE);
        }
        return memberRepository.findById(memberId)
                .orElseThrow(() -> MemberNotFoundException.of(MemberErrorCode.NOT_FOUND));
    }

    // 계좌 유효성 검증
    private Account validateAccount(Member member, Long accountId, Long competitionId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> AccountNotFoundException.of(AccountErrorCode.NOT_FOUND));
        if(account.getMember() != member) {
            throw AccountNotFoundException.of(AccountErrorCode.INVALID_VALUE);
        }
        if(!account.getCompetition().getId().equals(competitionId)) {
            throw AccountNotFoundException.of(AccountErrorCode.INVALID_VALUE);
        }
        return account;
    }

    // 원장 유효성 검증
    private History validateHistory(Account account, Long historyId) {
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> HistoryNotFoundException.of(HistoryErrorCode.NOT_FOUND));
        if(!history.getAccount().equals(account)) {
            throw AccountNotFoundException.of(AccountErrorCode.INVALID_VALUE);
        }

        final HistoryStatus[] validStatus = new HistoryStatus[]{
                HistoryStatus.지정가매수요청,
                HistoryStatus.지정가매도요청,
                HistoryStatus.시장가매수요청,
                HistoryStatus.시장가매도요청
        };
        if(!Arrays.asList(validStatus).contains(history.getStatus())) {
            throw HistoryNotFoundException.of(HistoryErrorCode.INVALID_VALUE);
        }
        return history;
    }
}

