package com.finance.dashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;

/** Summary plus per-category breakdown (ADMIN / ANALYST). */
public record DashboardAnalyticsResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        List<CategoryTotalResponse> categoryTotals) {
}
