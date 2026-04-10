package com.marketplace.payment.domain.model;

import com.marketplace.payment.domain.valueobject.IntegrationStatus;
import com.marketplace.payment.domain.valueobject.PaymentProvider;
import com.marketplace.payment.domain.valueobject.SecretReference;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class PaymentConnectorTest {

    private PaymentConnector newConnector() {
        return PaymentConnector.register(
            "tenant-1",
            PaymentProvider.MERCADO_PAGO,
            "MP Sandbox",
            SecretReference.of("mp-client-secret", "enc-123"),
            true,
            Set.of("BRL"),
            Set.of("BR"),
            "https://callback.example.com/mp"
        );
    }

    @Test
    void shouldRegisterWithPendingStatus() {
        PaymentConnector connector = newConnector();

        assertThat(connector.getStatus()).isEqualTo(IntegrationStatus.PENDING);
        assertThat(connector.getProvider()).isEqualTo(PaymentProvider.MERCADO_PAGO);
        assertThat(connector.getSupportedCurrencies()).contains("BRL");
    }

    @Test
    void shouldTransitionStatuses() {
        PaymentConnector connector = newConnector();

        connector.activate();
        assertThat(connector.getStatus()).isEqualTo(IntegrationStatus.ACTIVE);

        connector.disable("manual");
        assertThat(connector.getStatus()).isEqualTo(IntegrationStatus.DISABLED);

        connector.markError("timeout");
        assertThat(connector.getStatus()).isEqualTo(IntegrationStatus.ERROR);
        assertThat(connector.getErrorMessage()).isEqualTo("timeout");
    }

    @Test
    void shouldRotateSecret() {
        PaymentConnector connector = newConnector();
        var oldSecret = connector.getApiSecret();

        connector.rotateSecret("enc-456");

        assertThat(connector.getApiSecret().getEncryptedValue()).isEqualTo("enc-456");
        assertThat(connector.getApiSecret().getRotatedAt())
            .isAfterOrEqualTo(oldSecret.getRotatedAt());
    }
}
