package com.marketplace.payment.domain.valueobject;

/**
 * Supported external payment providers.
 */
public enum PaymentProvider {
    STRIPE,
    PAYPAL,
    MERCADO_PAGO,
    PAGSEGURO,
    ADYEN;
}
