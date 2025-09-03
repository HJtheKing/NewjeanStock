package com.sascom.chickenstock.domain.ranking.util;

import com.sascom.chickenstock.domain.account.entity.Account;

import java.util.*;

/**
 * Codeforces 스타일 Elo 변형을 적용한 랭킹/레이팅 계산기 (Refactored)
 *
 * - 실제 등수(타이 → 평균등수) 부여
 * - Seed(예상등수) = 1 + Σ P(상대가 나를 이길 확률)
 * - 목표등수 target = sqrt(seed × actualRank)
 * - getPerformanceRating: 자기 자신 제외하고 이분 탐색
 * - Δ = (performanceRating - beforeRating) / 2  (댐핑)
 * - (옵션) Δ 합계 보정으로 인플레/디플레 완화
 */
public class RatingCalculatorV2 {

    public static final int INITIAL_RATING = 1200;
    private static final int RATING_LOWER_BOUND = 0;
    private static final int RATING_UPPER_BOUND = 5000;

    // 컨테스트별 총 Δ 합을 0에 가깝게 보정할지 여부
    private static final boolean APPLY_SUM_TO_ZERO_SHIFT = true;

    /**
     * 현재 레이팅과 대회별 Δ 목록으로 새로운 레이팅 계산
     */
    public static int calculateNewRating(int currentRating, List<Integer> deltas) {
        if (deltas == null || deltas.isEmpty()) return currentRating;
        int r = currentRating;
        for (int d : deltas) r += d;
        return r;
    }

    /**
     * 컨테스트 결과를 받아 각 참가자의 등수/레이팅 변화량을 업데이트한 Account 리스트를 반환.
     * 입력: 참가자별 '대회 전 레이팅'
     * 정렬 기준: account.getBalance() 내림차순
     */
    public List<Account> processCompetitionResult(Map<Account, Integer> accountBeforeRatingMap) {
        // (1) 입력 변환
        final List<Participant> participants = accountBeforeRatingMap.entrySet()
                .stream()
                .map(e -> new Participant(e.getKey(), e.getValue()))
                .toList();

        if (participants.isEmpty()) return Collections.emptyList();

        // (2) 대회 성과 기준 정렬 (내림차순)
        participants.sort(Comparator.comparingLong((Participant p) -> p.account.getBalance()).reversed());

        // (3) 실제 등수 부여 (동점 → 평균등수)
        // ranking: UI/표시용 정수 등수, actualRank: 계산용 평균등수(double)
        for (int i = 0; i < participants.size(); ) {
            int j = i;
            long score = participants.get(i).account.getBalance();
            while (j < participants.size() && participants.get(j).account.getBalance() == score) j++;

            double avgRank = (i + 1 + j) / 2.0; // 1-based 평균 등수
            for (int k = i; k < j; k++) {
                Participant pk = participants.get(k);
                pk.ranking = (int) Math.floor(avgRank); // 필요 시 표시용으로 내림
                pk.actualRank = avgRank;                // 계산은 실수 등수 사용
                pk.expectedRanking = 1.0;               // seed 초기값(1-based)
            }
            i = j;
        }

        // (4) 예상 등수(Seed) = 1 + Σ P(other beats me)
        final int n = participants.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                Participant a = participants.get(i), b = participants.get(j);
                a.expectedRanking += eloWinProbability(b.beforeRating, a.beforeRating); // b beats a
                b.expectedRanking += eloWinProbability(a.beforeRating, b.beforeRating); // a beats b
            }
        }

        // (5) 퍼포먼스 레이팅 추정 → Δ 계산
        for (Participant p : participants) {
            double targetRank = Math.sqrt(p.expectedRanking * p.actualRank);  // 기하평균
            int perf = getPerformanceRating(targetRank, participants, p);
            p.ratingChange = (perf - p.beforeRating) / 2; // 감쇠(댐핑)
        }

        // (6) (옵션) Δ 합계 보정: 총합이 0이 되도록 근사 이동
        if (APPLY_SUM_TO_ZERO_SHIFT) {
            int sum = 0;
            for (Participant p : participants) sum += p.ratingChange;
            int nPart = participants.size();
            // 소수점 반올림으로 과도한 편향 방지(정밀 분배가 필요하면 잔여치 재분배 로직 추가)
            int shift = (int) Math.round(-sum / (double) nPart);
            for (Participant p : participants) p.ratingChange += shift;
        }

        // (7) Account 반영 및 반환
        List<Account> resultList = new ArrayList<>(participants.size());
        for (Participant p : participants) {
            p.account.updateRankingAndRatingChange(p.ranking, p.ratingChange);
            resultList.add(p.account);
        }
        return resultList;
    }

    /**
     * 목표 등수(targetRank)를 만족하는 '가상의 레이팅'을 이분 탐색으로 찾는다.
     * self(자기 자신)는 비교군에서 제외해야 함.
     */
    private int getPerformanceRating(double targetRank, List<Participant> participants, Participant self) {
        int left = RATING_LOWER_BOUND, right = RATING_UPPER_BOUND;

        while (right - left > 1) {
            int mid = (left + right) / 2;

            double midExpected = 1.0; // 1-based
            for (Participant other : participants) {
                if (other == self) continue; // 자기 자신 제외
                midExpected += eloWinProbability(other.beforeRating, mid); // other beats me(mid)
            }

            if (midExpected > targetRank) {
                left = mid;
            } else {
                right = mid;
            }
        }
        return left;
    }

    /**
     * Elo 승률: A가 B를 이길 확률
     */
    private double eloWinProbability(int ratingA, int ratingB) {
        return 1.0 / (1.0 + Math.pow(10.0, (ratingB - ratingA) / 400.0));
    }

    /**
     * 내부 참가자 모델
     */
    private static class Participant {
        private final Account account;
        private final int beforeRating;

        private int ranking;             // 표시용 정수 등수
        private double actualRank;       // 계산용 평균등수(타이 처리)
        private double expectedRanking;  // seed
        private int ratingChange;        // Δ

        private Participant(Account account, Integer beforeRating) {
            this.account = account;
            this.beforeRating = beforeRating == null ? INITIAL_RATING : beforeRating;
        }
    }
}
