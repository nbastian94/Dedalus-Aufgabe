package com.example.dedalusaufgabe.calculation.model;

import java.util.List;

public record Calculation(long amountInCents, List<DenominationCount> breakdown) {
}
