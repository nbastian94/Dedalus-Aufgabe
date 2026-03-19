package com.example.dedalusaufgabe.calculation.exception;

/**
 * Signals invalid input data or inconsistent calculation data in the domain workflow.
 */
public class InvalidCalculationException extends RuntimeException {
    public InvalidCalculationException(String message) {
        super(message);
    }
}
