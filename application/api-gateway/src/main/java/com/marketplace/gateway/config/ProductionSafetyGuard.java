package com.marketplace.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Fails fast on unsafe production configuration instead of booting silently:
 *
 * 1. JWT secret — with the dev default, forging an admin token is trivial.
 *    In prod, JWT_SECRET is mandatory and must differ from the default.
 * 2. Mock escrow — matchIfMissing=true means a forgotten flag would put the
 *    FAKE PSP in front of real users. In prod the mock only boots with the
 *    explicit Fase-0 opt-in (marketplace.escrow.allow-mock-in-prod=true),
 *    which exists so the no-money phase can run before a PSP adapter exists.
 */
@Component
public class ProductionSafetyGuard implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ProductionSafetyGuard.class);

    private final Environment environment;
    private final String jwtSecret;
    private final boolean escrowMockEnabled;
    private final boolean allowMockInProd;

    public ProductionSafetyGuard(
        Environment environment,
        @Value("${jwt.secret:" + JwtTokenProvider.DEV_DEFAULT_SECRET + "}") String jwtSecret,
        @Value("${marketplace.escrow.mock:true}") boolean escrowMockEnabled,
        @Value("${marketplace.escrow.allow-mock-in-prod:false}") boolean allowMockInProd
    ) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
        this.escrowMockEnabled = escrowMockEnabled;
        this.allowMockInProd = allowMockInProd;
    }

    @Override
    public void afterPropertiesSet() {
        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (!prod) {
            return;
        }
        if (jwtSecret == null || jwtSecret.isBlank() || JwtTokenProvider.DEV_DEFAULT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException(
                "JWT_SECRET is mandatory in the prod profile and must not be the dev default — "
                    + "with the known secret, forging an admin token is trivial.");
        }
        if (escrowMockEnabled && !allowMockInProd) {
            throw new IllegalStateException(
                "MockEscrowGateway must not run in the prod profile (Fase 1 gate: real PSP adapter). "
                    + "For a Fase-0 (no-money) deployment set marketplace.escrow.allow-mock-in-prod=true explicitly.");
        }
        if (escrowMockEnabled) {
            log.warn("Mock escrow ENABLED in prod by explicit Fase-0 opt-in — no real money may flow.");
        }
    }
}
