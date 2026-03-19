package com.example.dedalusaufgabe.calculation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Transfer object for the number of pieces used for a specific denomination.
 */
public record DenominationCountDto(
        @NotBlank(message = "denomination is required")
        String denomination,
        @Min(value = 0, message = "count must be >= 0")
        int count
) {
}
