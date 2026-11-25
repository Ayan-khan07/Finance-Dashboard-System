package com.finance.dashboard.entity;

import com.finance.dashboard.enums.RecordType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "financial_records",
        indexes = {
                @Index(name = "idx_records_user_id", columnList = "user_id"),
                @Index(name = "idx_records_date", columnList = "record_date"),
                @Index(name = "idx_records_category", columnList = "category"),
                @Index(name = "idx_records_type_date", columnList = "type, record_date")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FinancialRecord extends BaseEntity {

    /** The user who created / owns the record. LAZY: never loaded unless explicitly fetched. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordType type;

    @Column(nullable = false, length = 100)
    private String category;

    /** "date" is a SQL keyword, so the column is record_date. */
    @Column(name = "record_date", nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String notes;
}
