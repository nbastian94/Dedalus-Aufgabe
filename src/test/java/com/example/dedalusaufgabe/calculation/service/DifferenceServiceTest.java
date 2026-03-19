package com.example.dedalusaufgabe.calculation.service;

import com.example.dedalusaufgabe.calculation.model.DenominationCount;
import com.example.dedalusaufgabe.calculation.model.EuroDenomination;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DifferenceServiceTest {

    private final DifferenceService service = new DifferenceService();

    @Test
    void calculatesDifferenceForGivenExample() {
        List<DenominationCount> previous = List.of(
                new DenominationCount(EuroDenomination.TWENTY, 2),
                new DenominationCount(EuroDenomination.FIVE, 1),
                new DenominationCount(EuroDenomination.TWENTY_CENTS, 1),
                new DenominationCount(EuroDenomination.TEN_CENTS, 1),
                new DenominationCount(EuroDenomination.TWO_CENTS, 1)
        );
        List<DenominationCount> current = List.of(
                new DenominationCount(EuroDenomination.TWO_HUNDRED, 1),
                new DenominationCount(EuroDenomination.TWENTY, 1),
                new DenominationCount(EuroDenomination.TEN, 1),
                new DenominationCount(EuroDenomination.TWO, 2),
                new DenominationCount(EuroDenomination.TWENTY_CENTS, 1),
                new DenominationCount(EuroDenomination.TWO_CENTS, 1),
                new DenominationCount(EuroDenomination.ONE_CENT, 1)
        );

        Map<String, Integer> diffMap = service.calculateDifference(previous, current).stream()
                .collect(Collectors.toMap(
                        item -> item.denomination().display(),
                        item -> item.delta()
                ));

        assertEquals(1, diffMap.get("200.00"));
        assertEquals(-1, diffMap.get("20.00"));
        assertEquals(1, diffMap.get("10.00"));
        assertEquals(-1, diffMap.get("5.00"));
        assertEquals(2, diffMap.get("2.00"));
        assertEquals(0, diffMap.get("0.20"));
        assertEquals(-1, diffMap.get("0.10"));
        assertEquals(0, diffMap.get("0.02"));
        assertEquals(1, diffMap.get("0.01"));
    }
}
