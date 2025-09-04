package com.sascom.chickenstock.domain.history.service;

import com.sascom.chickenstock.domain.history.entity.History;
import com.sascom.chickenstock.domain.history.entity.HistoryStatus;
import com.sascom.chickenstock.domain.history.entity.TradeHistory;
import com.sascom.chickenstock.domain.history.repository.HistoryRepository;
import com.sascom.chickenstock.domain.history.repository.TradeHistoryRepository;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class HistoryService {

    private HistoryRepository historyRepository;
    private TradeHistoryRepository tradeHistoryRepository;

    @Autowired
    public HistoryService(HistoryRepository historyRepository, TradeHistoryRepository tradeHistoryRepository){
        this.historyRepository = historyRepository;
        this.tradeHistoryRepository = tradeHistoryRepository;
    }

    @Transactional
    public void save(History request){
        History history = new History(
                request.getAccount(),
                request.getCompany(),
                request.getPrice(),
                request.getPrice(),
                request.getStatus(),
                request.getOrderId()
        );
        historyRepository.save(history);
    }

    @Transactional
    public TradeHistory saveTradeHistory(TradeHistory history) {
        return tradeHistoryRepository.save(history);
    }
}
