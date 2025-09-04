package com.sascom.chickenstock.domain.orderbook.util;

@FunctionalInterface
public interface FillHandler {
    void onFill(FillEvent event);
}