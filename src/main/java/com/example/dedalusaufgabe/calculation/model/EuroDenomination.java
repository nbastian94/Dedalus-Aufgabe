package com.example.dedalusaufgabe.calculation.model;

import com.example.dedalusaufgabe.calculation.exception.InvalidCalculationException;

import java.util.Arrays;
import java.util.List;

/**
 * Supported euro denominations with integer cent values and display strings.
 */
public enum EuroDenomination {
    TWO_HUNDRED(20_000, "200.00"),
    ONE_HUNDRED(10_000, "100.00"),
    FIFTY(5_000, "50.00"),
    TWENTY(2_000, "20.00"),
    TEN(1_000, "10.00"),
    FIVE(500, "5.00"),
    TWO(200, "2.00"),
    ONE(100, "1.00"),
    HALF(50, "0.50"),
    TWENTY_CENTS(20, "0.20"),
    TEN_CENTS(10, "0.10"),
    FIVE_CENTS(5, "0.05"),
    TWO_CENTS(2, "0.02"),
    ONE_CENT(1, "0.01");

    private static final List<EuroDenomination> DESCENDING = List.of(values());

    private final int cents;
    private final String display;

    EuroDenomination(int cents, String display) {
        this.cents = cents;
        this.display = display;
    }

    public int cents() {
        return cents;
    }

    public String display() {
        return display;
    }

    public static List<EuroDenomination> descending() {
        return DESCENDING;
    }

    public static EuroDenomination fromDisplay(String displayValue) {
        return Arrays.stream(values())
                .filter(value -> value.display.equals(displayValue))
                .findFirst()
                .orElseThrow(() -> new InvalidCalculationException("Unknown denomination: " + displayValue));
    }
}
