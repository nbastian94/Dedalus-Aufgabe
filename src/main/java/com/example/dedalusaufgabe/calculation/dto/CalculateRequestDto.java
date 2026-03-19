package com.example.dedalusaufgabe.calculation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CalculateRequestDto(
        @NotBlank(message = "amount is required")
        @Pattern(
                regexp = "^\\d+(\\.\\d{1,2})?$",
                message = "amount must be a decimal value with up to two fractional digits"
        )
        String amount
) {
}
