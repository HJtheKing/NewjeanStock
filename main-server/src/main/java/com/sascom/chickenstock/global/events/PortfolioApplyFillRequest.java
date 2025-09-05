package com.sascom.chickenstock.global.events;

public record PortfolioApplyFillRequest(Long orderId, Long accountId, Long companyId, String side, Long price, Long quantity) {}
