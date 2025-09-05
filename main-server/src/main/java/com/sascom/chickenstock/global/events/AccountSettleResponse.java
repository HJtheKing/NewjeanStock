package com.sascom.chickenstock.global.events;

public record AccountSettleResponse(Long orderId, Long settledAmount, Long preReservedConsumed, Long borrowedConsumed, Long balanceDelta) {}
