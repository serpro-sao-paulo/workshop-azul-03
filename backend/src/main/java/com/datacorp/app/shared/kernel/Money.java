package com.datacorp.app.shared.kernel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object for monetary amounts.
 *
 * <p>REQ-021: Valores financeiros SEMPRE truncados (nunca arredondados) a 2 casas decimais.
 * source_legacy: CALCBENF.NSN, CALCDSCT.NSN, BATCHPGT.NSN (OQ-06 resolved: truncate)
 *
 * <p>Immutable. All arithmetic operations return new instances.
 * {@link #of(BigDecimal)} allows any sign; {@link #ofStrict(BigDecimal)} rejects negatives.
 */
public final class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));

    private final BigDecimal amount; // always scale=2, truncated

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    /** Creates Money truncating to 2 decimal places (REQ-021). */
    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount não pode ser nulo");
        return new Money(amount.setScale(2, RoundingMode.DOWN));
    }

    /** Creates Money truncating to 2 decimal places; rejects negative values. */
    public static Money ofStrict(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount não pode ser nulo");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money valor não pode ser negativo: " + amount);
        }
        return new Money(amount.setScale(2, RoundingMode.DOWN));
    }

    public static Money zero() {
        return ZERO;
    }

    /** Returns the amount with scale=2. */
    public BigDecimal amount() {
        return amount;
    }

    public Money add(Money other) {
        return of(amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return of(amount.subtract(other.amount));
    }

    public Money multiply(BigDecimal factor) {
        return of(amount.multiply(factor));
    }

    /** Returns true if this amount is negative. */
    public boolean isNegative() {
        return amount.signum() < 0;
    }

    /** Floors negative values to zero; returns this if non-negative. */
    public Money floorToZero() {
        return isNegative() ? ZERO : this;
    }

    /** Returns true if this > other. */
    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
