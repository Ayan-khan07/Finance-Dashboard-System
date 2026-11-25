package com.finance.dashboard.mapper;

import com.finance.dashboard.dto.response.CategoryTotalResponse;
import com.finance.dashboard.dto.response.RecordResponse;
import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.repository.projection.CategoryTotal;

public final class RecordMapper {

    private RecordMapper() {
    }

    /** The owner must already be loaded (the repository fetch-joins it) or the call must be inside a transaction. */
    public static RecordResponse toResponse(FinancialRecord record) {
        return new RecordResponse(
                record.getId(),
                record.getAmount(),
                record.getType(),
                record.getCategory(),
                record.getDate(),
                record.getNotes(),
                record.getUser().getId(),
                record.getUser().getUsername());
    }

    public static CategoryTotalResponse toCategoryTotalResponse(CategoryTotal total) {
        return new CategoryTotalResponse(total.getCategory(), total.getType(), total.getTotal());
    }
}
