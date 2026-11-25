package com.finance.dashboard.controller;

import com.finance.dashboard.dto.response.MonthlySummaryResponse;
import com.finance.dashboard.service.AiSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Insights")
public class AiController {

    private final AiSummaryService aiSummaryService;

    @GetMapping("/monthly-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "One-month financial summary with an AI-written health assessment and budgeting tips",
            description = "If the AI provider is slow or down, the figures are still returned with "
                    + "aiGenerated=false and a fallback message.")
    public MonthlySummaryResponse monthlySummary(@RequestParam int year, @RequestParam int month) {
        return aiSummaryService.getMonthlySummary(year, month);
    }
}
