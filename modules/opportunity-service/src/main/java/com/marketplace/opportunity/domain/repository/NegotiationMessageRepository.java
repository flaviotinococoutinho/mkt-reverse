package com.marketplace.opportunity.domain.repository;

import com.marketplace.opportunity.domain.model.NegotiationMessage;
import com.marketplace.opportunity.domain.model.Opportunity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NegotiationMessageRepository extends JpaRepository<NegotiationMessage, UUID> {
    List<NegotiationMessage> findByOpportunityOrderByCreatedAtAsc(Opportunity opportunity);
}
