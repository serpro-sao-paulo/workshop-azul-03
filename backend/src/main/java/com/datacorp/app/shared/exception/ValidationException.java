package com.datacorp.app.shared.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exception for accumulative validation errors (REQ-002: não curto-circuito).
 * source_legacy: VALBENEF.NSN#L113-L165 — collects all errors before returning.
 */
public class ValidationException extends DomainException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("VALIDATION_ERROR", "Erros de validação: " + String.join(", ", errors));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public ValidationException(String error) {
        this(List.of(error));
    }

    public List<String> errors() {
        return errors;
    }
}
