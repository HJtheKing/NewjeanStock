package com.sascom.chickenstock.domain.history.entity;


import com.sascom.chickenstock.domain.account.entity.Account;
import com.sascom.chickenstock.domain.company.entity.Company;
import com.sascom.chickenstock.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "history")
public class History extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @NotNull
    @Column(name = "price")
    private Long price;

    @NotNull
    @Column(name = "volume")
    private Long volume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private HistoryStatus status;

    @NotNull
    @Column(name = "orderId")
    private Long orderId;

    @Builder
    public History(Account account, Company company, Long price, Long volume, HistoryStatus status, Long orderId) {
        this.account = account;
        this.company = company;
        this.price = price;
        this.volume = volume;
        this.status = status;
        this.orderId = orderId;
    }
}

