package com.datacorp.app.shared.kernel;

import java.util.Objects;

/**
 * Value object for CPF (Cadastro de Pessoa Física).
 *
 * <p>Centralizes Módulo 11 validation and masking logic per:
 * <ul>
 *   <li>REQ-001: CPF obrigatório e válido por Módulo 11</li>
 *   <li>REQ-003: Rejeição de CPF com dígitos repetidos</li>
 *   <li>REQ-015: CPF mascarado em logs (short-CPF normalization)</li>
 * </ul>
 *
 * <p>source_legacy: VALBENEF.NSN#L113-L205, CADBENEF.NSN#L103-L115
 */
public final class Cpf {

    private final String value; // always 11 raw digits

    private Cpf(String value) {
        this.value = value;
    }

    /**
     * Creates a validated Cpf from any format (with or without punctuation).
     * Normalizes to 11 digits (left-pads with zeros for legacy short-CPFs).
     *
     * @throws NullPointerException     if cpf is null
     * @throws IllegalArgumentException if the CPF is invalid
     */
    public static Cpf of(String cpf) {
        Objects.requireNonNull(cpf, "CPF não pode ser nulo");
        String digits = cpf.replaceAll("[^\\d]", "");
        // Normalize: left-pad to 11 digits for legacy short CPFs (REQ-015)
        if (digits.length() < 11) {
            digits = String.format("%011d", Long.parseLong(digits));
        }
        validate(digits);
        return new Cpf(digits);
    }

    /** Raw 11-digit string, no punctuation. */
    public String value() {
        return value;
    }

    /**
     * Masked representation for logs and responses (REQ-015, Principle V).
     * Format: ***.ddd.ddd-** where ddd.ddd are digits 3-8.
     */
    public String masked() {
        // value is 11 digits: d0d1d2 d3d4d5 d6d7d8 d9d10
        // formatted: xxx.xxx.xxx-xx → ***.d3d4d5.d6d7d8-**
        return String.format("***.%s.%s-**",
                value.substring(3, 6),
                value.substring(6, 9));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cpf other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return masked(); // never expose raw CPF in logs/toString (Principle V)
    }

    // ── Validation ─────────────────────────────────────────────────────────

    private static void validate(String digits) {
        if (digits.length() != 11) {
            throw new IllegalArgumentException("CPF inválido: deve ter 11 dígitos");
        }
        // REQ-003: reject all-same digits
        if (digits.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF inválido: dígitos repetidos");
        }
        // REQ-001: Módulo 11 — first check digit
        if (!checkDigit(digits, 9)) {
            throw new IllegalArgumentException("CPF inválido: dígito verificador incorreto");
        }
        // REQ-001: Módulo 11 — second check digit
        if (!checkDigit(digits, 10)) {
            throw new IllegalArgumentException("CPF inválido: dígito verificador incorreto");
        }
    }

    private static boolean checkDigit(String digits, int position) {
        int sum = 0;
        for (int i = 0; i < position; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (position + 1 - i);
        }
        int remainder = (sum * 10) % 11;
        if (remainder == 10) remainder = 0;
        return remainder == Character.getNumericValue(digits.charAt(position));
    }
}
