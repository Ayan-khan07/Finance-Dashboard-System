package com.finance.dashboard.repository;

import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.enums.RecordType;
import com.finance.dashboard.repository.projection.CategoryTotal;
import com.finance.dashboard.repository.projection.TypeTotal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Retrieval (filter + pagination) goes through {@link JpaSpecificationExecutor};
 * every analytics figure is computed by the database with SUM() / GROUP BY.
 * Nothing here loads a full record set into memory to total it in Java.
 *
 * totalIncome / totalExpense / netBalance are derived by the service from the two
 * rows returned by {@code totalsByType} (net = income - expense).
 */
public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    // ------------------------------------------------------------------
    // Retrieval
    // ------------------------------------------------------------------

    /** Fetch-joins the owner so mapping a page to DTOs does not trigger N+1 lazy loads. */
    @Override
    @EntityGraph(attributePaths = "user")
    Page<FinancialRecord> findAll(Specification<FinancialRecord> spec, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Optional<FinancialRecord> findWithUserById(Long id);

    // ------------------------------------------------------------------
    // Analytics: all-time (dashboard overview)
    // ------------------------------------------------------------------

    @Query("""
            SELECT r.type AS type, SUM(r.amount) AS total
            FROM FinancialRecord r
            GROUP BY r.type
            """)
    List<TypeTotal> totalsByType();

    @Query("""
            SELECT r.category AS category, r.type AS type, SUM(r.amount) AS total
            FROM FinancialRecord r
            GROUP BY r.category, r.type
            ORDER BY SUM(r.amount) DESC
            """)
    List<CategoryTotal> categoryTotals();

    // ------------------------------------------------------------------
    // Analytics: date-bounded (inclusive on both ends), used for monthly summaries
    // ------------------------------------------------------------------

    @Query("""
            SELECT r.type AS type, SUM(r.amount) AS total
            FROM FinancialRecord r
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY r.type
            """)
    List<TypeTotal> totalsByType(@Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT r.category AS category, r.type AS type, SUM(r.amount) AS total
            FROM FinancialRecord r
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY r.category, r.type
            ORDER BY SUM(r.amount) DESC
            """)
    List<CategoryTotal> categoryTotals(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * Top-N categories for one record type in a period; the limit comes from the Pageable
     * (e.g. {@code PageRequest.of(0, 3)}), so only N rows leave the database.
     */
    @Query("""
            SELECT r.category AS category, r.type AS type, SUM(r.amount) AS total
            FROM FinancialRecord r
            WHERE r.type = :type
              AND r.date BETWEEN :startDate AND :endDate
            GROUP BY r.category, r.type
            ORDER BY SUM(r.amount) DESC
            """)
    List<CategoryTotal> topCategoriesByType(@Param("type") RecordType type,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            Pageable pageable);
}
