package com.finance.dashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * The figures are always computed from the database. {@code aiSummary} is the Gemini assessment when
 * {@code aiGenerated} is true; otherwise it carries a fallback message and the figures are still valid.
 */
public record MonthlySummaryResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netSavings,
        List<CategoryTotalResponse> topSpendingCategories,
        String aiSummary,
        boolean aiGenerated) {
}
