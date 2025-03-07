package com.sascom.chickenstock.domain.socket;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sascom.chickenstock.domain.company.dto.response.CompanyInfoResponse;
import com.sascom.chickenstock.domain.company.entity.Company;
import com.sascom.chickenstock.domain.company.error.code.CompanyErrorCode;
import com.sascom.chickenstock.domain.company.error.exception.CompanyNotFoundException;
import com.sascom.chickenstock.domain.company.repository.CompanyRepository;
import com.sascom.chickenstock.domain.company.service.CompanyService;
import com.sascom.chickenstock.domain.trade.dto.RealStockTradeDto;
import com.sascom.chickenstock.domain.trade.dto.RealStockTradeDtoV2;
import com.sascom.chickenstock.domain.trade.dto.TradeType;
//import com.sascom.chickenstock.domain.trade.service.TradeService;
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
//    private final TradeService tradeService;
    private final TradeServiceV2 tradeService;
    private final CompanyService companyService;

//    @Autowired
//    public MyStompSessionHandler(TradeService tradeService, CompanyService companyService) {
//        objectMapper = new ObjectMapper();
//        this.tradeService = tradeService;
//        this.companyService = companyService;
//    }

    @Autowired
    public MyStompSessionHandler(TradeServiceV2 tradeService, CompanyService companyService) {
        objectMapper = new ObjectMapper();
        this.tradeService = tradeService;
        this.companyService = companyService;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
//        final String[] companyStockCodeList = {"005930", "009150", "000660", "299660", "042700",
//                "035420", "035720", "028300", "084650", "257720"};
//        for(String stockCode : companyStockCodeList) {
//            session.subscribe("/stock-purchase/" + stockCode, this);
//            logger.log(Level.INFO, "Connected and subscribed to /stock-purchase/{}", stockCode);
//        }
        List<CompanyInfoResponse> companyInfoResponses = companyService.getCompanyInfoList();
//        System.out.println(companyInfoResponses.size());
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

//    @Override
//    public void handleFrame(StompHeaders headers, Object payload) {
////        System.out.println("Received: " + payload.toString());
//        // Handle the received payload here
//        try {
//            RealStockTradeDto realStockTradeDto = parseMessage(payload.toString());
//            tradeService.processRealStockTrade(realStockTradeDto);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
//        System.out.println("Received: " + payload.toString());
        // Handle the received payload here
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

//    private RealStockTradeDto parseMessage(String message) throws JsonProcessingException {
//        JsonNode jsonNode = objectMapper.readTree(message);
//        return new RealStockTradeDto(
//                companyService.getCompanyIdByCode(jsonNode.get("stockCode").asText()),
//                jsonNode.get("currentPrice").asInt(),
//                jsonNode.get("transactionVolume").asInt(),
//                switch(jsonNode.get("transactionType").asInt()) {
//                    case 1 -> TradeType.BUY;
//                    case 5 -> TradeType.SELL;
//                    default -> throw new IllegalStateException("parseError");
//                });
//    }

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