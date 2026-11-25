package com.finance.dashboard.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/** Stable, framework-independent pagination envelope (PageImpl's JSON shape is not a supported contract). */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
