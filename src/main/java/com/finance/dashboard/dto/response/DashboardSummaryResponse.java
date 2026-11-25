package com.finance.dashboard.dto.response;

import java.math.BigDecimal;

/** High-level numbers only - the only dashboard data a VIEWER may see. */
public record DashboardSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance) {
}
