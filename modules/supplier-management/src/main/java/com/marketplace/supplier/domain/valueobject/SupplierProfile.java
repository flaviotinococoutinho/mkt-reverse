package com.marketplace.supplier.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Supplier company profile information.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SupplierProfile implements Serializable {

    @Column(name = "profile_website", length = 255)
    private String website;

    @Column(name = "profile_email", length = 255)
    private String primaryEmail;

    @Column(name = "profile_phone", length = 30)
    private String primaryPhone;

    @Column(name = "profile_country", length = 2)
    private String country;

    @Column(name = "profile_city", length = 100)
    private String city;

    @Column(name = "profile_industry", length = 120)
    private String industry;

    @Column(name = "profile_description", length = 500)
    private String description;

    @Column(name = "profile_employees")
    private Integer employeeCount;

    @Column(name = "profile_founded_in")
    private Integer foundedIn;

    public static SupplierProfile of(
        String website,
        String primaryEmail,
        String primaryPhone,
        String country,
        String city,
        String industry,
        String description,
        Integer employeeCount,
        Integer foundedIn
    ) {
        return new SupplierProfile(
            normalize(website),
            normalize(primaryEmail),
            normalize(primaryPhone),
            normalize(country),
            normalize(city),
            normalize(industry),
            description != null ? description.trim() : null,
            employeeCount,
            foundedIn
        );
    }

    private static String normalize(String value) {
        return value != null && !value.trim().isEmpty() ? value.trim() : null;
    }
}
