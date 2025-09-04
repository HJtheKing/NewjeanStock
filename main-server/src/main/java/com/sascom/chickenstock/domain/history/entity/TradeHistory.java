package com.sascom.chickenstock.domain.history.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trade_history")
public class TradeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 8)
    private String side; // "BUY" | "SELL"

    @Column(nullable = false)
    private Long price; // 체결가(원)

    @Column(nullable = false)
    private Long quantity; // 체결수량(주)

    @Column(nullable = false)
    private Boolean margin; // 증거금 여부

    @Column(nullable = false)
    private Boolean completed; // 이 체결로 주문이 완료되었는지

    @Column(nullable = false)
    private Instant executedAt; // 체결 시각
}