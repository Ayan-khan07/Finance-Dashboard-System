package com.finance.dashboard.dto.response;

import com.finance.dashboard.enums.RecordType;

import java.math.BigDecimal;

public record CategoryTotalResponse(
        String category,
        RecordType type,
        BigDecimal total) {
}
