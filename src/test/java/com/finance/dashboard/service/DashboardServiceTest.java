package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.DashboardAnalyticsResponse;
import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.enums.RecordType;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.repository.FinancialRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void summaryDerivesNetBalanceFromDatabaseTotals() {
        when(recordRepository.totalsByType()).thenReturn(List.of(
                TestRows.typeTotal(RecordType.INCOME, "1000.50"),
                TestRows.typeTotal(RecordType.EXPENSE, "400.25")));

        DashboardSummaryResponse summary = dashboardService.getSummary(null, null);

        assertEquals(new BigDecimal("1000.50"), summary.totalIncome());
        assertEquals(new BigDecimal("400.25"), summary.totalExpense());
        assertEquals(new BigDecimal("600.25"), summary.netBalance());
    }

    @Test
    void missingTypeCountsAsZero() {
        when(recordRepository.totalsByType()).thenReturn(List.of(
                TestRows.typeTotal(RecordType.INCOME, "250.00")));

        DashboardSummaryResponse summary = dashboardService.getSummary(null, null);

        assertEquals(new BigDecimal("0.00"), summary.totalExpense());
        assertEquals(new BigDecimal("250.00"), summary.netBalance());
    }

    @Test
    void rangedSummaryUsesDateBoundedQuery() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        when(recordRepository.totalsByType(start, end)).thenReturn(List.of(
                TestRows.typeTotal(RecordType.EXPENSE, "75.00")));

        DashboardSummaryResponse summary = dashboardService.getSummary(start, end);

        assertEquals(new BigDecimal("-75.00"), summary.netBalance());
    }

    @Test
    void analyticsIncludesCategoryBreakdown() {
        when(recordRepository.totalsByType()).thenReturn(List.of(
                TestRows.typeTotal(RecordType.EXPENSE, "300.00")));
        when(recordRepository.categoryTotals()).thenReturn(List.of(
                TestRows.categoryTotal("Rent", RecordType.EXPENSE, "200.00"),
                TestRows.categoryTotal("Food", RecordType.EXPENSE, "100.00")));

        DashboardAnalyticsResponse analytics = dashboardService.getAnalytics(null, null);

        assertEquals(2, analytics.categoryTotals().size());
        assertEquals("Rent", analytics.categoryTotals().get(0).category());
        assertEquals(new BigDecimal("-300.00"), analytics.netBalance());
    }

    @Test
    void singleDateBoundIsRejected() {
        assertThrows(BadRequestException.class,
                () -> dashboardService.getSummary(LocalDate.of(2026, 1, 1), null));
    }

    @Test
    void startAfterEndIsRejected() {
        assertThrows(BadRequestException.class,
                () -> dashboardService.getSummary(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)));
    }
}
