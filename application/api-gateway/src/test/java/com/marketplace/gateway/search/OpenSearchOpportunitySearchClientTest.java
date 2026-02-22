package com.marketplace.gateway.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.gateway.api.SourcingMvpController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenSearchOpportunitySearchClientTest {

    @Test
    void parses_hits_into_page_result() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:9201");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        String payload = """
            {
              "hits": {
                "total": {"value": 2, "relation": "eq"},
                "hits": [
                  {"_source": {"id": "1", "status": "PUBLISHED", "title": "Pneu", "description": "", "eventType": "RFQ", "tenantId": "t1", "buyerOrganizationId": "b1"}},
                  {"_source": {"id": "2", "status": "PUBLISHED", "title": "Bateria", "description": "", "eventType": "RFQ", "tenantId": "t1", "buyerOrganizationId": "b1"}}
                ]
              }
            }
            """;

        server.expect(method(org.springframework.http.HttpMethod.POST))
            .andExpect(requestTo("http://localhost:9201/opportunities/_search"))
            .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        // build client using same RestClient via builder
        OpenSearchOpportunitySearchClient client = new OpenSearchOpportunitySearchClient(restClient, new ObjectMapper(), "opportunities");

        var result = client.search(new OpportunitySearchRequest(
            "t1",
            "supplier-1",
            5533,
            "pneu",
            "ALL",
            "PUBLICATION_AT",
            "DESC",
            0,
            10
        ));

        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.items()).hasSize(2);
        SourcingMvpController.SourcingEventView first = result.items().getFirst();
        assertThat(first.id()).isEqualTo("1");

        server.verify();
    }
}
