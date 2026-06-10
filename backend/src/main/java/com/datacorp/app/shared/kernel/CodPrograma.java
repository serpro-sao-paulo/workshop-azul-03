package com.datacorp.app.shared.kernel;

import java.util.Objects;

/**
 * Value object for CodPrograma (código de programa social, A4 = 4-char alphanumeric).
 * DDM: AA COD-PROGRAMA A 4 in PROGRAMA-SOCIAL.ddm
 * source_legacy: CADPROG.NSN, CONSBENEF.NSN, BATCHPGT.NSN
 */
public final class CodPrograma {

    private static final int MAX_LENGTH = 4;

    private final String value;

    private CodPrograma(String value) {
        this.value = value;
    }

    /** Creates a CodPrograma, trimming and upper-casing the value. */
    public static CodPrograma of(String value) {
        Objects.requireNonNull(value, "CodPrograma não pode ser nulo");
        String trimmed = value.trim().toUpperCase();
        if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "CodPrograma inválido: deve ter 1-4 caracteres: '" + value + "'");
        }
        return new CodPrograma(trimmed);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodPrograma other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
