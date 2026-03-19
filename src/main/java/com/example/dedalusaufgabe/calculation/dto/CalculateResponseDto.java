package com.example.dedalusaufgabe.calculation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CalculateResponseDto(
        String amount,
        List<DenominationCountDto> breakdown,
        List<DifferenceDto> difference
) {
}
