package com.finance.dashboard.dto.response;

import com.finance.dashboard.enums.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordResponse(
        Long id,
        BigDecimal amount,
        RecordType type,
        String category,
        LocalDate date,
        String notes,
        Long userId,
        String createdBy) {
}
