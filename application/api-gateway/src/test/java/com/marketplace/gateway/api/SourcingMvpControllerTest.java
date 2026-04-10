package com.marketplace.gateway.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SourcingMvpControllerTest {

    private String createEvent(String tenantId, String orgId, String title) throws Exception {
        var createEvent = new java.util.LinkedHashMap<String, Object>();
        createEvent.put("tenantId", tenantId);
        createEvent.put("buyerOrganizationId", orgId);
        createEvent.put("buyerContactName", "Comprador");
        createEvent.put("buyerContactPhone", "+5527999999999");
        createEvent.put("title", title);
        createEvent.put("description", "Detalhes");
        createEvent.put("mccCategoryCode", 5533);
        createEvent.put("productName", "Peça X");
        createEvent.put("productDescription", "Compatível com Y");
        createEvent.put("category", "part");
        createEvent.put("unitOfMeasure", "UN");
        createEvent.put("quantityRequired", 1);
        createEvent.put("attributes", java.util.List.of(
            java.util.Map.of("key", "voltage", "type", "VOLTAGE", "unit", "V", "value", 220),
            java.util.Map.of("key", "color", "type", "COLOR", "value", "preto")
        ));
        createEvent.put("validForHours", 24);
        createEvent.put("estimatedBudgetCents", 50000);

        var eventResp = mvc.perform(post("/api/v1/sourcing-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createEvent)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(eventResp).get("id").asText();
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void health_is_ok() throws Exception {
        mvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().exists("X-Correlation-Id"))
            .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void can_list_events_with_filters() throws Exception {
        String eventIdTenant1 = createEvent("tenant-1", "org-1", "Quero a peça X");
        createEvent("tenant-2", "org-2", "Quero a peça Y");

        mvc.perform(get("/api/v1/sourcing-events")
                .accept(MediaTypes.HAL_JSON)
                .param("tenantId", "tenant-1")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.sourcingEventViewList[0].id").value(eventIdTenant1))
            .andExpect(jsonPath("$._embedded.sourcingEventViewList[0]._links.self.href").isString())
            .andExpect(jsonPath("$._embedded.sourcingEventViewList[0]._links.responses.href").isString())
            .andExpect(jsonPath("$._links.self.href").isString())
            .andExpect(jsonPath("$.page.size").value(10))
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.totalElements").isNumber())
            .andExpect(jsonPath("$.page.totalPages").isNumber());
    }

    @Test
    void supplier_can_search_opportunities_directory() throws Exception {
        createEvent("tenant-1", "org-1", "Notebook corporativo i7");
        String wanted = createEvent("tenant-1", "org-1", "Compro pneu 195/55 R15");

        mvc.perform(get("/api/v1/opportunities")
                .accept(MediaTypes.HAL_JSON)
                .param("tenantId", "tenant-1")
                .param("supplierId", "supplier-1")
                .param("q", "pneu")
                .param("visibility", "ALL")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.sourcingEventViewList[0].id").value(wanted))
            .andExpect(jsonPath("$._embedded.sourcingEventViewList[0]._links.self.href").isString())
            .andExpect(jsonPath("$._embedded.sourcingEventViewList[0]._links.responses.href").isString())
            .andExpect(jsonPath("$._links.self.href").isString())
            .andExpect(jsonPath("$.page.size").value(10))
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.totalElements").isNumber())
            .andExpect(jsonPath("$.page.totalPages").isNumber());
    }

    @Test
    void supplier_can_search_opportunities_with_fuzzy_endpoint_fallbacking_to_postgres_when_opensearch_disabled() throws Exception {
        createEvent("tenant-1", "org-1", "Notebook corporativo i7");
        String wanted = createEvent("tenant-1", "org-1", "Compro pneu 195/55 R15");

        mvc.perform(get("/api/v1/opportunities/search")
                .accept(MediaTypes.HAL_JSON)
                .param("tenantId", "tenant-1")
                .param("supplierId", "supplier-1")
                .param("q", "pneu")
                .param("visibility", "ALL")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.sourcingEventViewList[0].id").value(wanted))
            .andExpect(jsonPath("$.page.totalElements").isNumber());
    }

    @Test
    void can_create_event_and_submit_offer() throws Exception {
        String eventId = createEvent("tenant-1", "org-1", "Quero a peça X");

        var offer = new java.util.LinkedHashMap<String, Object>();
        offer.put("supplierId", "supplier-1");
        offer.put("offerCents", 19900);
        offer.put("leadTimeDays", 3);
        offer.put("warrantyMonths", 3);
        offer.put("condition", "USED");
        offer.put("shippingMode", "PICKUP");
        offer.put("attributes", java.util.List.of(
            java.util.Map.of("key", "voltage", "type", "VOLTAGE", "unit", "V", "value", 220)
        ));
        offer.put("message", "Tenho em estoque");

        mvc.perform(post("/api/v1/sourcing-events/" + eventId + "/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offer)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isString())
            .andExpect(jsonPath("$._links.responses.href").isString())
            .andExpect(jsonPath("$._links.accept.href").isString());

        var responseList = mvc.perform(get("/api/v1/sourcing-events/" + eventId + "/responses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].supplierId").value("supplier-1"))
            .andExpect(jsonPath("$[0].id").isString())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String responseId = objectMapper.readTree(responseList).get(0).get("id").asText();

        mvc.perform(post("/api/v1/sourcing-events/" + eventId + "/responses/" + responseId + "/accept"))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/sourcing-events/" + eventId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("AWARDED"));
    }

    @Test
    void returns_problem_details_on_validation_error() throws Exception {
        var createEvent = new java.util.LinkedHashMap<String, Object>();
        createEvent.put("tenantId", "tenant-1");
        createEvent.put("buyerOrganizationId", "org-1");
        createEvent.put("buyerContactName", "Comprador");
        createEvent.put("buyerContactPhone", "+5527999999999");
        createEvent.put("title", "Quero a peça X");
        createEvent.put("description", "Detalhes");
        createEvent.put("mccCategoryCode", 5533);
        createEvent.put("productName", "Peça X");
        createEvent.put("unitOfMeasure", "UN");
        createEvent.put("quantityRequired", 1);

        // invalid attribute key for category 5533 (should fail HARD normalization)
        createEvent.put("attributes", java.util.List.of(
            java.util.Map.of("key", "__invalid__", "type", "TEXT", "value", "x")
        ));
        createEvent.put("validForHours", 24);

        mvc.perform(post("/api/v1/sourcing-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createEvent)))
            .andExpect(status().isBadRequest())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().exists("X-Correlation-Id"))
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.correlationId").isString());
    }
}
