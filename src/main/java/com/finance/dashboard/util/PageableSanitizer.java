package com.finance.dashboard.util;

import com.finance.dashboard.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Clients control "sort" through the query string. Without a whitelist an unknown property
 * becomes a 500 (PropertyReferenceException) and nested paths (e.g. user.password) could be probed.
 * A deterministic "id" tie-breaker keeps pagination stable when many rows share the sort value.
 */
public final class PageableSanitizer {

    private PageableSanitizer() {
    }

    public static Pageable sanitize(Pageable pageable, List<String> allowedProperties, Sort defaultSort) {
        Sort sort = pageable.getSort();

        for (Sort.Order order : sort) {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new BadRequestException("Unsupported sort property '" + order.getProperty()
                        + "'. Allowed: " + String.join(", ", allowedProperties));
            }
        }
        if (sort.isUnsorted()) {
            sort = defaultSort;
        }
        if (sort.getOrderFor("id") == null) {
            sort = sort.and(Sort.by(Sort.Direction.DESC, "id"));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
