package com.marketplace.shared.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object representing monetary amounts with currency. Immutable and
 * self-validating; avoids Lombok to keep compilation predictable across modules.
 */
@Embeddable
public class Money {

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    @NotNull
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private CurrencyCode currency;

    protected Money() {
        // for JPA
    }

    private Money(BigDecimal amount, CurrencyCode currency) {
        this.amount = amount;
        this.currency = currency;
    }

    @JsonCreator
    public static Money of(@JsonProperty("amount") BigDecimal amount,
                           @JsonProperty("currency") CurrencyCode currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        if (amount.scale() > 4) {
            amount = amount.setScale(4, RoundingMode.HALF_UP);
        }
        return new Money(amount, currency);
    }

    public static Money of(double amount, CurrencyCode currency) {
        return of(BigDecimal.valueOf(amount), currency);
    }

    public static Money of(long amount, CurrencyCode currency) {
        return of(BigDecimal.valueOf(amount), currency);
    }

    public static Money zero(CurrencyCode currency) {
        return of(BigDecimal.ZERO, currency);
    }

    public static Money brl(BigDecimal amount) {
        return of(amount, CurrencyCode.BRL);
    }

    public static Money usd(BigDecimal amount) {
        return of(amount, CurrencyCode.USD);
    }

    public static Money eur(BigDecimal amount) {
        return of(amount, CurrencyCode.EUR);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        if (factor == null) {
            throw new IllegalArgumentException("Factor cannot be null");
        }
        return new Money(this.amount.multiply(factor).setScale(4, RoundingMode.HALF_UP), this.currency);
    }

    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    public Money divide(BigDecimal divisor) {
        if (divisor == null) {
            throw new IllegalArgumentException("Divisor cannot be null");
        }
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return new Money(this.amount.divide(divisor, 4, RoundingMode.HALF_UP), this.currency);
    }

    public Money divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    public Money abs() {
        return new Money(this.amount.abs(), this.currency);
    }

    public Money negate() {
        return new Money(this.amount.negate(), this.currency);
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }

    public int compareTo(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    public Currency getCurrencyInstance() {
        return Currency.getInstance(currency.name());
    }

    public String format() {
        return String.format("%s %s", currency.getSymbol(), amount.toPlainString());
    }

    private void validateSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Other money cannot be null");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Cannot operate on different currencies: %s and %s",
                    this.currency, other.currency));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && currency == money.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return format();
    }
}
