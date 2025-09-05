package com.sascom.chickenstock.global.events;

public record AccountCompensationEvent(Long orderId, Long accountId, Long companyId, String side, String reason, Long occurredAt) {}
