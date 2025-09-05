package com.sascom.chickenstock.global.events;

public record AccountSettleRequest(Long orderId, Long accountId, Long companyId, String side, Long price, Long quantity, Boolean margin) {}
