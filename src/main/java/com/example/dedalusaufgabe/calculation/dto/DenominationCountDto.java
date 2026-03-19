package com.example.dedalusaufgabe.calculation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DenominationCountDto(
        @NotBlank(message = "denomination is required")
        String denomination,
        @Min(value = 0, message = "count must be >= 0")
        int count
) {
}
