package com.marketplace.gateway.graphql;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

@SpringBootTest
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class SourcingGraphqlTest {

    @Autowired
    GraphQlTester graphQlTester;

    @Test
    void can_create_event_submit_offer_and_accept_via_graphql() {
        String createMutation = """
            mutation($input: CreateSourcingEventInput!) {
              createSourcingEvent(input: $input)
            }
            """;

        var input = new java.util.LinkedHashMap<String, Object>();
        input.put("tenantId", "tenant-1");
        input.put("buyerOrganizationId", "org-1");
        input.put("buyerContactName", "Comprador");
        input.put("buyerContactPhone", "+5527999999999");
        input.put("title", "Quero a peça X");
        input.put("description", "Detalhes");
        input.put("type", "RFQ");
        input.put("mccCategoryCode", 5533);
        input.put("productName", "Peça X");
        input.put("productDescription", "Compatível com Y");
        input.put("category", "part");
        input.put("unitOfMeasure", "UN");
        input.put("quantityRequired", 1);
        input.put("attributes", java.util.List.of(
            Map.of("key", "voltage", "type", "VOLTAGE", "unit", "V", "value", 220),
            Map.of("key", "color", "type", "COLOR", "value", "preto")
        ));
        input.put("validForHours", 24);
        input.put("estimatedBudgetCents", 50000);

        String eventId = graphQlTester
            .document(createMutation)
            .variable("input", input)
            .execute()
            .path("createSourcingEvent")
            .entity(String.class)
            .get();

        String submitMutation = """
            mutation($input: SubmitResponseInput!) {
              submitResponse(input: $input)
            }
            """;

        String responseId = graphQlTester
            .document(submitMutation)
            .variable("input", Map.of(
                "eventId", eventId,
                "supplierId", "supplier-1",
                "offerCents", 19900,
                "leadTimeDays", 3,
                "warrantyMonths", 3,
                "condition", "USED",
                "shippingMode", "PICKUP",
                "attributes", java.util.List.of(
                    Map.of("key", "voltage", "type", "VOLTAGE", "unit", "V", "value", 220)
                ),
                "message", "Tenho em estoque"
            ))
            .execute()
            .path("submitResponse")
            .entity(String.class)
            .get();

        String acceptMutation = """
            mutation($eventId: ID!, $responseId: ID!) {
              acceptResponse(eventId: $eventId, responseId: $responseId)
            }
            """;

        graphQlTester
            .document(acceptMutation)
            .variable("eventId", eventId)
            .variable("responseId", responseId)
            .execute()
            .path("acceptResponse")
            .entity(Boolean.class)
            .isEqualTo(true);

        String query = """
            query($id: ID!) {
              sourcingEvent(id: $id) { id status }
            }
            """;

        graphQlTester
            .document(query)
            .variable("id", eventId)
            .execute()
            .path("sourcingEvent.status")
            .entity(String.class)
            .isEqualTo("AWARDED");

        String listQuery = """
            query($tenantId: String!, $page: Int!, $size: Int!) {
              sourcingEvents(tenantId: $tenantId, page: $page, size: $size) { id status }
            }
            """;

        graphQlTester
            .document(listQuery)
            .variable("tenantId", "tenant-1")
            .variable("page", 0)
            .variable("size", 10)
            .execute()
            .path("sourcingEvents")
            .entityList(SourcingGraphqlController.SourcingEventView.class)
            .satisfies(list -> {
                if (list.stream().noneMatch(e -> e.id().equals(eventId))) {
                    throw new AssertionError("Expected sourcingEvents to contain eventId=" + eventId);
                }
            });
    }

    @Test
    void supplier_can_query_opportunities_directory() {
        String createMutation = """
            mutation($input: CreateSourcingEventInput!) {
              createSourcingEvent(input: $input)
            }
            """;

        var input = new java.util.LinkedHashMap<String, Object>();
        input.put("tenantId", "tenant-1");
        input.put("buyerOrganizationId", "org-1");
        input.put("buyerContactName", "Comprador");
        input.put("buyerContactPhone", "+5527999999999");
        input.put("title", "Compro pneu 195/55 R15");
        input.put("description", "Detalhes");
        input.put("type", "RFQ");
        input.put("mccCategoryCode", 5533);
        input.put("productName", "Pneu");
        input.put("productDescription", "");
        input.put("category", "part");
        input.put("unitOfMeasure", "UN");
        input.put("quantityRequired", 1);
        input.put("attributes", java.util.List.of(
            Map.of("key", "voltage", "type", "VOLTAGE", "unit", "V", "value", 220),
            Map.of("key", "color", "type", "COLOR", "value", "preto")
        ));
        input.put("validForHours", 24);

        String eventId = graphQlTester
            .document(createMutation)
            .variable("input", input)
            .execute()
            .path("createSourcingEvent")
            .entity(String.class)
            .get();

        String query = """
            query($supplierId: String!, $tenantId: String, $q: String) {
              opportunitiesForSupplier(supplierId: $supplierId, tenantId: $tenantId, q: $q, visibility: "ALL", page: 0, size: 10) { id status }
            }
            """;

        graphQlTester
            .document(query)
            .variable("supplierId", "supplier-1")
            .variable("tenantId", "tenant-1")
            .variable("q", "pneu")
            .execute()
            .path("opportunitiesForSupplier")
            .entityList(SourcingGraphqlController.SourcingEventView.class)
            .satisfies(list -> {
                if (list.stream().noneMatch(e -> e.id().equals(eventId))) {
                    throw new AssertionError("Expected opportunitiesForSupplier to contain eventId=" + eventId);
                }
            });
    }

    @Test
    void graphql_errors_include_code_and_correlation_id_extensions() {
        String createMutation = """
            mutation($input: CreateSourcingEventInput!) {
              createSourcingEvent(input: $input)
            }
            """;

        var input = new java.util.LinkedHashMap<String, Object>();
        input.put("tenantId", "tenant-1");
        input.put("buyerOrganizationId", "org-1");
        input.put("buyerContactName", "Comprador");
        input.put("buyerContactPhone", "+5527999999999");
        input.put("title", "Quero a peça X");
        input.put("description", "Detalhes");
        input.put("type", "RFQ");
        input.put("mccCategoryCode", 5533);
        input.put("productName", "Peça X");
        input.put("productDescription", "Compatível com Y");
        input.put("category", "part");
        input.put("unitOfMeasure", "UN");
        input.put("quantityRequired", 1);
        input.put("attributes", java.util.List.of(
            // invalid type triggers IllegalArgumentException (valueOf)
            Map.of("key", "voltage", "type", "NOT_A_REAL_TYPE", "unit", "V", "value", 220)
        ));
        input.put("validForHours", 24);

        graphQlTester
            .document(createMutation)
            .variable("input", input)
            .execute()
            .errors()
            .satisfy(errors -> {
                if (errors.isEmpty()) {
                    throw new AssertionError("Expected GraphQL errors but got none");
                }
                var extensions = errors.getFirst().getExtensions();
                if (extensions == null) {
                    throw new AssertionError("Expected GraphQL error extensions to be present");
                }
                if (!"VALIDATION_ERROR".equals(extensions.get("code"))) {
                    throw new AssertionError("Expected extensions.code=VALIDATION_ERROR but got " + extensions.get("code"));
                }
                if (extensions.get("correlationId") == null) {
                    throw new AssertionError("Expected extensions.correlationId to be present");
                }
            });
    }
}
