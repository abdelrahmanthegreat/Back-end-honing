package masr;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money implements Comparable<Money>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final int SCALE = 2;
    public static final int PIastres_PER_UNIT = 100;

    public static final Money ZERO = new Money(0L);

    private final long piastres;

    private Money(long piastres) {
        this.piastres = piastres;
    }

    public static Money ofEgp(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        return new Money(amount.movePointRight(SCALE).setScale(0, RoundingMode.HALF_UP).longValueExact());
    }

    public static Money ofEgp(String amount) {
        return ofEgp(new BigDecimal(Objects.requireNonNull(amount, "amount must not be null").trim()));
    }

    public long piastres() {
        return piastres;
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(piastres, SCALE);
    }

    public Money plus(Money other) {
        return new Money(piastres + Objects.requireNonNull(other, "other must not be null").piastres);
    }

    public Money minus(Money other) {
        return new Money(piastres - Objects.requireNonNull(other, "other must not be null").piastres);
    }

    public Money times(long factor) {
        return new Money(Math.multiplyExact(piastres, factor));
    }

    public Money times(BigDecimal factor) {
        Objects.requireNonNull(factor, "factor must not be null");
        return ofEgp(toBigDecimal().multiply(factor));
    }

    public Money timesRate(BigDecimal rate) {
        return times(rate);
    }

    public Money min(Money other) {
        return compareTo(other) <= 0 ? this : other;
    }

    public Money max(Money other) {
        return compareTo(other) >= 0 ? this : other;
    }

    public Money atLeast(Money floor) {
        return max(floor);
    }

    public boolean isZero() {
        return piastres == 0L;
    }

    public boolean isPositive() {
        return piastres > 0L;
    }

    public boolean isNegative() {
        return piastres < 0L;
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(piastres, other.piastres);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money other)) {
            return false;
        }
        return piastres == other.piastres;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(piastres);
    }

    public String format() {
        return String.format("%,.2f", toBigDecimal());
    }

    @Override
    public String toString() {
        return format() + " EGP";
    }
}
