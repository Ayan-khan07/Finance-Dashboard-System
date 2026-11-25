package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.MonthlySummaryResponse;
import com.finance.dashboard.enums.RecordType;
import com.finance.dashboard.exception.AiServiceException;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.repository.FinancialRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSummaryServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @Mock
    private GeminiClient geminiClient;

    @InjectMocks
    private AiSummaryService aiSummaryService;

    private void stubOctoberData() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);
        when(recordRepository.totalsByType(start, end)).thenReturn(List.of(
                TestRows.typeTotal(RecordType.INCOME, "5000.00"),
                TestRows.typeTotal(RecordType.EXPENSE, "3200.00")));
        when(recordRepository.topCategoriesByType(eq(RecordType.EXPENSE), eq(start), eq(end), any(Pageable.class)))
                .thenReturn(List.of(TestRows.categoryTotal("Rent", RecordType.EXPENSE, "1600.00")));
    }

    @Test
    void returnsAiTextAndPassesStructuredFiguresToGemini() {
        stubOctoberData();
        when(geminiClient.generate(anyString(), anyString())).thenReturn("Looks healthy.");

        MonthlySummaryResponse response = aiSummaryService.getMonthlySummary(2026, 10);

        assertTrue(response.aiGenerated());
        assertEquals("Looks healthy.", response.aiSummary());
        assertEquals(new BigDecimal("1800.00"), response.netSavings());
        assertEquals("Rent", response.topSpendingCategories().get(0).category());

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(geminiClient).generate(eq(AiSummaryService.SYSTEM_PROMPT), prompt.capture());
        assertTrue(prompt.getValue().contains("October 2026"));
        assertTrue(prompt.getValue().contains("Net savings: 1800.00"));
        assertTrue(prompt.getValue().contains("Savings rate: 36.0%"));
        assertTrue(prompt.getValue().contains("Rent: 1600.00 (50.0%)"));
    }

    @Test
    void fallsBackGracefullyWhenGeminiFails() {
        stubOctoberData();
        when(geminiClient.generate(anyString(), anyString())).thenThrow(new AiServiceException("timeout"));

        MonthlySummaryResponse response = aiSummaryService.getMonthlySummary(2026, 10);

        assertFalse(response.aiGenerated());
        assertEquals(AiSummaryService.FALLBACK_MESSAGE, response.aiSummary());
        assertEquals(new BigDecimal("5000.00"), response.totalIncome());
    }

    @Test
    void skipsGeminiWhenMonthHasNoRecords() {
        MonthlySummaryResponse response = aiSummaryService.getMonthlySummary(2026, 3);

        assertFalse(response.aiGenerated());
        assertEquals(AiSummaryService.NO_DATA_MESSAGE, response.aiSummary());
        verifyNoInteractions(geminiClient);
    }

    @Test
    void rejectsInvalidMonthAndYear() {
        assertThrows(BadRequestException.class, () -> aiSummaryService.getMonthlySummary(2026, 13));
        assertThrows(BadRequestException.class, () -> aiSummaryService.getMonthlySummary(2026, 0));
        assertThrows(BadRequestException.class, () -> aiSummaryService.getMonthlySummary(1850, 5));
    }
}
