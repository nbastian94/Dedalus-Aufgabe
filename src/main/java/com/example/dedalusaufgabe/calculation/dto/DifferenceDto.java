package com.example.dedalusaufgabe.calculation.dto;

/**
 * Transfer object describing how a denomination count changed versus the previous calculation.
 */
public record DifferenceDto(String denomination, int delta) {
}
