package com.example.dedalusaufgabe.calculation.util;

import com.example.dedalusaufgabe.calculation.exception.InvalidCalculationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    private MoneyUtils() {
    }

    public static long parseAmountToCents(String amount, boolean mustBePositive) {
        final BigDecimal parsed;
        try {
            parsed = new BigDecimal(amount.trim());
        } catch (NumberFormatException exception) {
            throw new InvalidCalculationException("Invalid amount: " + amount);
        }

        if (parsed.scale() > 2) {
            throw new InvalidCalculationException("Amount must have at most two fractional digits");
        }
        if (parsed.signum() < 0 || (mustBePositive && parsed.signum() == 0)) {
            throw new InvalidCalculationException("Amount must be greater than zero");
        }

        return parsed
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact();
    }

    public static String centsToAmount(long cents) {
        return BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }
}
