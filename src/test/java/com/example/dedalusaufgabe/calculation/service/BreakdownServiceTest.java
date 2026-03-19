package com.example.dedalusaufgabe.calculation.service;

import com.example.dedalusaufgabe.calculation.model.DenominationCount;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BreakdownServiceTest {

    private final BreakdownService service = new BreakdownService();

    @Test
    void calculatesExpectedBreakdownForSampleValue() {
        Map<String, Integer> result = service.calculateBreakdown(23_423).stream()
                .collect(Collectors.toMap(
                        item -> item.denomination().display(),
                        DenominationCount::count
                ));

        assertEquals(1, result.get("200.00"));
        assertEquals(1, result.get("20.00"));
        assertEquals(1, result.get("10.00"));
        assertEquals(2, result.get("2.00"));
        assertEquals(1, result.get("0.20"));
        assertEquals(1, result.get("0.02"));
        assertEquals(1, result.get("0.01"));
    }

    @Test
    void returnsEmptyForZeroAmount() {
        assertTrue(service.calculateBreakdown(0).isEmpty());
    }
}
