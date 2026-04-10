package com.marketplace.opportunity.domain.repository;

import com.marketplace.opportunity.domain.model.Opportunity;
import com.marketplace.opportunity.domain.OpportunityStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OpportunityRepository extends JpaRepository<Opportunity, UUID>, JpaSpecificationExecutor<Opportunity> {
}
