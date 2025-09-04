package com.sascom.chickenstock.domain.history.repository;

import com.sascom.chickenstock.domain.history.entity.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {}
