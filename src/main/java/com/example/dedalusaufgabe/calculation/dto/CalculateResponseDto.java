package com.example.dedalusaufgabe.calculation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response payload with calculated breakdown and optional delta to the previous calculation.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CalculateResponseDto(
        String amount,
        List<DenominationCountDto> breakdown,
        List<DifferenceDto> difference
) {
}
