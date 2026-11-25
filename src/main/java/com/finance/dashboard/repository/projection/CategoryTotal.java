package com.finance.dashboard.repository.projection;

import com.finance.dashboard.enums.RecordType;

import java.math.BigDecimal;

/** One row of: SELECT category, type, SUM(amount) ... GROUP BY category, type. */
public interface CategoryTotal {

    String getCategory();

    RecordType getType();

    BigDecimal getTotal();
}
