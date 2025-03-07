package com.sascom.chickenstock.domain.account.dto.response;

import java.util.List;

public record UnexecutedStockInfoResponseV2(
        List<UnexecutedStockInfoV2> unexecution
) {
}
