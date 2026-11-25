package com.finance.dashboard.service;

import com.finance.dashboard.repository.projection.TypeTotal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Income / expense totals for a period. The database has already done the SUM()/GROUP BY;
 * this just maps the (at most two) result rows and derives the net figure.
 * A record type with no rows in the period is zero.
 */
public record PeriodTotals(BigDecimal income, BigDecimal expense) {

    public static PeriodTotals from(List<TypeTotal> rows) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (TypeTotal row : rows) {
            if (row.getTotal() == null || row.getType() == null) {
                continue;
            }
            switch (row.getType()) {
                case INCOME -> income = row.getTotal();
                case EXPENSE -> expense = row.getTotal();
            }
        }
        return new PeriodTotals(
                income.setScale(2, RoundingMode.HALF_UP),
                expense.setScale(2, RoundingMode.HALF_UP));
    }

    /** income - expense (negative when spending exceeds income). */
    public BigDecimal net() {
        return income.subtract(expense);
    }
}
