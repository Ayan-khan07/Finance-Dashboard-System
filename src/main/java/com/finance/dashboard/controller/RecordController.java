package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.RecordRequest;
import com.finance.dashboard.dto.response.PageResponse;
import com.finance.dashboard.dto.response.RecordResponse;
import com.finance.dashboard.enums.RecordType;
import com.finance.dashboard.security.CustomUserDetails;
import com.finance.dashboard.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Reads: ADMIN + ANALYST. Writes: ADMIN only. VIEWER has no access to individual records. */
@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
@Tag(name = "Financial Records")
public class RecordController {

    private final RecordService recordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "List records with optional filters, pagination and sorting",
            description = "Sortable fields: id, amount, type, category, date. Example: sort=amount,desc")
    public PageResponse<RecordResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @ParameterObject @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return recordService.list(category, type, startDate, endDate, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get a record by id")
    public RecordResponse get(@PathVariable Long id) {
        return recordService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a record (ADMIN)")
    public RecordResponse create(@Valid @RequestBody RecordRequest request,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        return recordService.create(request, currentUser.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Replace a record's fields (ADMIN)")
    public RecordResponse update(@PathVariable Long id, @Valid @RequestBody RecordRequest request) {
        return recordService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a record (ADMIN)")
    public void delete(@PathVariable Long id) {
        recordService.delete(id);
    }
}
