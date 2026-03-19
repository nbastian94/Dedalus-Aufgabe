package com.example.dedalusaufgabe.calculation.service;

import com.example.dedalusaufgabe.calculation.model.Calculation;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Stores the most recent calculation in-memory for difference computation.
 */
@Service
public class CalculationStoreService {

    private final AtomicReference<Calculation> lastCalculation = new AtomicReference<>();

    public Optional<Calculation> getLastCalculation() {
        return Optional.ofNullable(lastCalculation.get());
    }

    public void saveCalculation(Calculation calculation) {
        lastCalculation.set(calculation);
    }
}
