package com.datacorp.app.shared.exception;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response body for all REST errors.
 * source_legacy: uniform error contract across all contexts.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        List<String> errors,
        String path
) {
    public static ErrorResponse of(int status, String errorCode, String message, String path) {
        return new ErrorResponse(Instant.now(), status, errorCode, message, List.of(), path);
    }

    public static ErrorResponse ofErrors(int status, String errorCode, String message,
                                          List<String> errors, String path) {
        return new ErrorResponse(Instant.now(), status, errorCode, message, errors, path);
    }
}
