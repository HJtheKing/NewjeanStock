package com.sascom.chickenstock.domain.account.dto.response;

public record StockInfoV2(
        Long companyId,
        Long price,
        Long volume
) {

}
