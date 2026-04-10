package com.marketplace.opportunity.domain.specification;

import com.marketplace.opportunity.domain.OpportunityStatus;
import com.marketplace.opportunity.domain.model.Opportunity;
import org.springframework.data.jpa.domain.Specification;

public final class OpportunitySpecifications {

    private OpportunitySpecifications() {}

    public static Specification<Opportunity> hasStatus(OpportunityStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Opportunity> hasCategory(String category) {
        return (root, query, cb) -> category == null || category.isBlank()
                ? null
                : cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    public static Specification<Opportunity> hasLocation(String location) {
        return (root, query, cb) -> location == null || location.isBlank()
                ? null
                : cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<Opportunity> search(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank()) {
                return null;
            }
            String like = "%" + term.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }
}
