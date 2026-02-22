package com.marketplace.opportunity.domain.repository;

import com.marketplace.opportunity.domain.model.Bid;
import com.marketplace.opportunity.domain.model.Opportunity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, UUID> {
    List<Bid> findByOpportunity(Opportunity opportunity);
}
