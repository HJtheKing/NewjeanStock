package com.sascom.chickenstock.global.events;

public record PortfolioCompensationEvent(Long orderId, Long accountId, Long companyId, String side, String reason, Long occurredAt) {}
