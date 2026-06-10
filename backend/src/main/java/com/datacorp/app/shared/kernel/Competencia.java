package com.datacorp.app.shared.kernel;

import java.util.Objects;

/**
 * Value object for competência (referência de pagamento, formato AAAAMM).
 *
 * <p>Represents the year-month reference used across Payments and Folha operations.
 * source_legacy: BATCHPGT.NSN, BATCHCON.NSN (DT-COMPETENCIA fields)
 */
public final class Competencia {

    private final int year;
    private final int month;

    private Competencia(int year, int month) {
        this.year = year;
        this.month = month;
    }

    /**
     * Creates a Competencia from a 6-digit integer (AAAAMM format).
     * e.g. 202601 = January 2026
     */
    public static Competencia of(int aaaamm) {
        int year = aaaamm / 100;
        int month = aaaamm % 100;
        validate(year, month);
        return new Competencia(year, month);
    }

    /** Creates a Competencia from year and month. */
    public static Competencia of(int year, int month) {
        validate(year, month);
        return new Competencia(year, month);
    }

    private static void validate(int year, int month) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Competência inválida: ano=" + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Competência inválida: mês=" + month);
        }
    }

    /** Returns AAAAMM as integer. */
    public int toInt() {
        return year * 100 + month;
    }

    public int year() { return year; }
    public int month() { return month; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Competencia other)) return false;
        return year == other.year && month == other.month;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month);
    }

    @Override
    public String toString() {
        return String.format("%04d%02d", year, month);
    }
}
