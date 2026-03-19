package com.example.dedalusaufgabe.calculation.model;

/**
 * Internal domain model for the delta between two denomination breakdowns.
 */
public record DenominationDelta(EuroDenomination denomination, int delta) {
}
