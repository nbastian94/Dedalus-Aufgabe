package com.example.dedalusaufgabe.calculation.model;

import java.util.List;

/**
 * Internal domain model representing an amount and its denomination breakdown in cents.
 */
public record Calculation(long amountInCents, List<DenominationCount> breakdown) {
}
