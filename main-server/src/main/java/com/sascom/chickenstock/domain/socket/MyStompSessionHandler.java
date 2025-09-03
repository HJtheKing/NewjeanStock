package com.sascom.chickenstock.domain.socket;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sascom.chickenstock.domain.company.dto.response.CompanyInfoResponse;
import com.sascom.chickenstock.domain.company.service.CompanyService;
import com.sascom.chickenstock.domain.trade.dto.RealStockTradeDtoV2;
import com.sascom.chickenstock.domain.trade.dto.TradeType;
import com.sascom.chickenstock.domain.trade.service.TradeServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@Component
public class MyStompSessionHandler extends StompSessionHandlerAdapter {

    private static final Logger logger = Logger.getLogger(MyStompSessionHandler.class.getName());
    private final ObjectMapper objectMapper;
    private final TradeServiceV2 tradeService;
    private final CompanyService companyService;


    @Autowired
    public MyStompSessionHandler(TradeServiceV2 tradeService, CompanyService companyService) {
        objectMapper = new ObjectMapper();
        this.tradeService = tradeService;
        this.companyService = companyService;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        List<CompanyInfoResponse> companyInfoResponses = companyService.getCompanyInfoList();
        for(CompanyInfoResponse companyInfoResponse : companyInfoResponses) {
            String stockCode = companyInfoResponse.code();
            log.info("url=/stock-purchase/{}", stockCode);
            try {
                session.subscribe("/stock-purchase/" + stockCode, this);
            }
            catch (Exception e) {
                log.error("error={}", e.getMessage());
            }
            logger.log(Level.INFO, "Connected and subscribed to /stock-purchase/{}", stockCode);
        }
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        try {
            RealStockTradeDtoV2 realStockTradeDto = parseMessage(payload.toString());
            log.warn("########## price:{},volume:{}",
                    realStockTradeDto.currentPrice(),
                    realStockTradeDto.transactionVolume());
            tradeService.processExecution(realStockTradeDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return String.class;
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.severe("Failure in WebSocket handling: " + exception.getMessage());
        throw new RuntimeException("Failure in WebSocket handling", exception);
    }

    private RealStockTradeDtoV2 parseMessage(String message) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(message);
        logger.log(Level.INFO, "Received JSON object: " + jsonNode.toString());
        return new RealStockTradeDtoV2(
                companyService.getCompanyIdByCode(jsonNode.get("stockCode").asText()),
                jsonNode.get("currentPrice").asLong(),
                jsonNode.get("transactionVolume").asLong(),
                switch(jsonNode.get("transactionType").asInt()) {
                    case 1 -> TradeType.BUY;
                    case 5 -> TradeType.SELL;
                    default -> throw new IllegalStateException("parseError");
                });
    }
}