package com.sascom.chickenstock.domain.account.dto.response;

import java.util.List;

public record AccountInfoResponseV2(
        Long balance,
        List<StockInfoV2> stocks
) {
}
