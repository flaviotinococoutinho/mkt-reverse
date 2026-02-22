package com.marketplace.shared.id;

/**
 * Port for generating unique identifiers.
 *
 * Keep this interface in shared-domain to avoid framework coupling.
 */
public interface IdGenerator {

    long nextId();
}

