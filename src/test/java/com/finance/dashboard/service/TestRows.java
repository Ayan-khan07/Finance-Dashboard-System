package com.finance.dashboard.service;

import com.finance.dashboard.enums.RecordType;
import com.finance.dashboard.repository.projection.CategoryTotal;
import com.finance.dashboard.repository.projection.TypeTotal;

import java.math.BigDecimal;

/** Hand-rolled projection rows for repository stubs. */
final class TestRows {

    private TestRows() {
    }

    static TypeTotal typeTotal(RecordType type, String amount) {
        return new TypeTotal() {
            @Override
            public RecordType getType() {
                return type;
            }

            @Override
            public BigDecimal getTotal() {
                return new BigDecimal(amount);
            }
        };
    }

    static CategoryTotal categoryTotal(String category, RecordType type, String amount) {
        return new CategoryTotal() {
            @Override
            public String getCategory() {
                return category;
            }

            @Override
            public RecordType getType() {
                return type;
            }

            @Override
            public BigDecimal getTotal() {
                return new BigDecimal(amount);
            }
        };
    }
}
