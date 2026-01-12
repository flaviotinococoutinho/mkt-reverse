package com.marketplace.opportunity.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value Object representing monetary value.
 * Encapsulates amount and currency following Martin Fowler's Money pattern.
 * 
 * Immutable and follows Object Calisthenics principles.
 */
public final class Money {
    
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    private final BigDecimal amount;
    private final Currency currency;
    
    private Money(BigDecimal amount, Currency currency) {
        this.amount = validateAndScale(amount);
        this.currency = validateCurrency(currency);
    }
    
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }
    
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }
    
    public static Money brl(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("BRL"));
    }
    
    public static Money usd(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("USD"));
    }
    
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
    
    /**
     * Adds another money value.
     * 
     * @param other money to add
     * @return new Money with sum
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }
    
    /**
     * Subtracts another money value.
     * 
     * @param other money to subtract
     * @return new Money with difference
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }
    
    /**
     * Multiplies by a factor.
     * 
     * @param factor multiplication factor
     * @return new Money with product
     */
    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor), currency);
    }
    
    /**
     * Divides by a divisor.
     * 
     * @param divisor division divisor
     * @return new Money with quotient
     */
    public Money divide(BigDecimal divisor) {
        if (isZero(divisor)) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return new Money(amount.divide(divisor, SCALE, ROUNDING_MODE), currency);
    }
    
    /**
     * Checks if this money is greater than another.
     * 
     * @param other money to compare
     * @return true if greater
     */
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Checks if this money is less than another.
     * 
     * @param other money to compare
     * @return true if less
     */
    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }
    
    /**
     * Checks if this money is zero.
     * 
     * @return true if zero
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Checks if this money is positive.
     * 
     * @return true if positive
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Checks if this money is negative.
     * 
     * @return true if negative
     */
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public BigDecimal amount() {
        return amount;
    }
    
    public Currency currency() {
        return currency;
    }
    
    public String currencyCode() {
        return currency.getCurrencyCode();
    }
    
    private BigDecimal validateAndScale(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        return amount.setScale(SCALE, ROUNDING_MODE);
    }
    
    private Currency validateCurrency(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        return currency;
    }
    
    private void validateSameCurrency(Money other) {
        if (hasDifferentCurrency(other)) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot operate on different currencies: %s and %s",
                    currency.getCurrencyCode(),
                    other.currency.getCurrencyCode()
                )
            );
        }
    }
    
    private boolean hasDifferentCurrency(Money other) {
        return !currency.equals(other.currency);
    }
    
    private boolean isZero(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (isNotMoney(other)) {
            return false;
        }
        
        Money that = (Money) other;
        return Objects.equals(amount, that.amount) 
            && Objects.equals(currency, that.currency);
    }
    
    private boolean isNotMoney(Object other) {
        return other == null || getClass() != other.getClass();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", currency.getSymbol(), amount);
    }
}
