package com.sascom.chickenstock.global.events;

public record AccountSettleStatusResponse(
        String status, Long settledAmount, Long preReservedConsumed, Long borrowedConsumed, Long balanceDelta
) {}