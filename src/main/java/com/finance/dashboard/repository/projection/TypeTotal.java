package com.finance.dashboard.repository.projection;

import com.finance.dashboard.enums.RecordType;

import java.math.BigDecimal;

/** One row of: SELECT type, SUM(amount) ... GROUP BY type. A type with no records yields no row. */
public interface TypeTotal {

    RecordType getType();

    BigDecimal getTotal();
}
