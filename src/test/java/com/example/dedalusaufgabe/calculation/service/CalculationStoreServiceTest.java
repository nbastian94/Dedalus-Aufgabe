package com.example.dedalusaufgabe.calculation.service;

import com.example.dedalusaufgabe.calculation.model.Calculation;
import com.example.dedalusaufgabe.calculation.model.DenominationCount;
import com.example.dedalusaufgabe.calculation.model.EuroDenomination;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculationStoreServiceTest {

    @Test
    void storesAndReturnsLastCalculation() {
        CalculationStoreService store = new CalculationStoreService();
        assertTrue(store.getLastCalculation().isEmpty());

        Calculation calculation = new Calculation(
                15_000,
                List.of(
                        new DenominationCount(EuroDenomination.ONE_HUNDRED, 1),
                        new DenominationCount(EuroDenomination.FIFTY, 1)
                )
        );

        store.saveCalculation(calculation);

        Calculation stored = store.getLastCalculation().orElseThrow();
        assertEquals(15_000, stored.amountInCents());
        assertEquals(2, stored.breakdown().size());
    }
}
