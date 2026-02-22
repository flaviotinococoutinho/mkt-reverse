package com.marketplace.supplier.domain.valueobject;

import java.util.Objects;

/**
 * The seller's legal nature for compliance/capability decisions.
 *
 * <p>In QueroJá, sellers can be individuals (PF) or businesses (PJ/MEI).
 */
public enum SellerNature {
    INDIVIDUAL,
    BUSINESS;

    public static SellerNature from(TaxIdentifier taxIdentifier) {
        Objects.requireNonNull(taxIdentifier, "taxIdentifier");
        return switch (taxIdentifier.getType()) {
            case CPF -> INDIVIDUAL;
            case CNPJ -> BUSINESS;
            default -> BUSINESS;
        };
    }
}

