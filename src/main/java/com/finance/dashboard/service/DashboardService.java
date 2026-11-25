package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.CategoryTotalResponse;
import com.finance.dashboard.dto.response.DashboardAnalyticsResponse;
import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.mapper.RecordMapper;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.projection.CategoryTotal;
import com.finance.dashboard.repository.projection.TypeTotal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Every figure comes from a SUM()/GROUP BY executed by the database. With no dates the
 * figures cover all records; with startDate AND endDate (inclusive) they cover that period.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        boolean ranged = validateRange(startDate, endDate);
        List<TypeTotal> rows = ranged
                ? recordRepository.totalsByType(startDate, endDate)
                : recordRepository.totalsByType();

        PeriodTotals totals = PeriodTotals.from(rows);
        return new DashboardSummaryResponse(totals.income(), totals.expense(), totals.net());
    }

    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getAnalytics(LocalDate startDate, LocalDate endDate) {
        boolean ranged = validateRange(startDate, endDate);
        List<TypeTotal> typeRows = ranged
                ? recordRepository.totalsByType(startDate, endDate)
                : recordRepository.totalsByType();
        List<CategoryTotal> categoryRows = ranged
                ? recordRepository.categoryTotals(startDate, endDate)
                : recordRepository.categoryTotals();

        PeriodTotals totals = PeriodTotals.from(typeRows);
        List<CategoryTotalResponse> categories = categoryRows.stream()
                .map(RecordMapper::toCategoryTotalResponse)
                .toList();

        return new DashboardAnalyticsResponse(totals.income(), totals.expense(), totals.net(), categories);
    }

    /** @return true when a date range was supplied, false for "all time" */
    private boolean validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return false;
        }
        if (startDate == null || endDate == null) {
            throw new BadRequestException("startDate and endDate must be provided together");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must not be after endDate");
        }
        return true;
    }
}
