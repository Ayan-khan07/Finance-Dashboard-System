package com.finance.dashboard.dto.request;

import com.finance.dashboard.enums.RecordType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Used for both create (POST) and full update (PUT) of a financial record. */
public record RecordRequest(
        @NotNull
        @Positive
        @Digits(integer = 13, fraction = 2, message = "must have at most 13 integer digits and 2 decimal places")
        BigDecimal amount,

        @NotNull
        RecordType type,

        @NotBlank
        @Size(max = 100)
        String category,

        @NotNull
        LocalDate date,

        @Size(max = 500)
        String notes) {
}
