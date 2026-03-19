package com.example.dedalusaufgabe.calculation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record CalculationDto(
        @NotBlank(message = "amount is required")
        @Pattern(
                regexp = "^\\d+(\\.\\d{1,2})?$",
                message = "amount must be a decimal value with up to two fractional digits"
        )
        String amount,
        @NotNull(message = "breakdown is required")
        List<@Valid DenominationCountDto> breakdown
) {
}
