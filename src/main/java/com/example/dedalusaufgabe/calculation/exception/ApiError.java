package com.example.dedalusaufgabe.calculation.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized API error payload used by the global exception handling layer.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
}
