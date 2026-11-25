package com.finance.dashboard.repository;

import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.enums.RecordType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the optional filters for GET /api/v1/records. Any null / blank argument is skipped,
 * so the same method serves every combination of category, type, startDate and endDate.
 * Dates are inclusive. Category matching is case-insensitive.
 */
public final class FinancialRecordSpecifications {

    private FinancialRecordSpecifications() {
    }

    public static Specification<FinancialRecord> withFilters(String category,
                                                             RecordType type,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<LocalDate>get("date"), endDate));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
