package com.marketplace.gateway.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end contract flow: acceptance forms the agreement, then
 * fund → ship → deliver → release (and the dispute/ceiling guardrails).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgreementFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    com.marketplace.shared.infrastructure.outbox.OutboxEventRepository outboxEventRepository;

    private static String buyerToken;
    private static String buyerUserId;
    private static String supplierToken;
    private static String supplierUserId;

    @BeforeEach
    void authenticate() throws Exception {
        if (buyerToken == null) {
            JsonNode buyer = registerOrLogin("agreement-buyer@example.com", "11144477735", "CPF", "BUYER");
            buyerToken = buyer.get("accessToken").asText();
            buyerUserId = buyer.get("user").get("id").asText();
        }
        if (supplierToken == null) {
            JsonNode supplier = registerOrLogin("agreement-supplier@example.com", "11222333000181", "CNPJ", "SUPPLIER");
            supplierToken = supplier.get("accessToken").asText();
            supplierUserId = supplier.get("user").get("id").asText();
        }
    }

    @Test
    void acceptanceOpensAgreementAndHappyPathReachesReleased() throws Exception {
        String eventId = createEvent("Quero action figure raro", 25_000L);
        String responseId = submitProposal(eventId, supplierUserId, 19_900L);

        var acceptResult = mvc.perform(post("/api/v1/sourcing-events/" + eventId + "/responses/" + responseId + "/accept")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isNoContent())
            .andReturn();

        String agreementId = acceptResult.getResponse().getHeader("X-Agreement-Id");
        assertThat(agreementId).isNotBlank();

        mvc.perform(get("/api/v1/agreements/" + agreementId)
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING_FUNDING"))
            .andExpect(jsonPath("$.buyerId").value(buyerUserId))
            .andExpect(jsonPath("$.supplierId").value(supplierUserId))
            .andExpect(jsonPath("$.priceCents").value(19900))
            .andExpect(jsonPath("$.snapshotHash").isNotEmpty());

        mvc.perform(post("/api/v1/agreements/" + agreementId + "/fund")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FUNDED"))
            .andExpect(jsonPath("$.escrowReference").isNotEmpty());

        mvc.perform(post("/api/v1/agreements/" + agreementId + "/ship")
                .header("Authorization", "Bearer " + supplierToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"trackingCode\":\"BR123456789XX\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SHIPPED"))
            .andExpect(jsonPath("$.trackingCode").value("BR123456789XX"));

        // The SELLER cannot self-declare delivery (fake tracking + inspection
        // window would auto-release the escrow without merchandise).
        mvc.perform(post("/api/v1/agreements/" + agreementId + "/deliver")
                .header("Authorization", "Bearer " + supplierToken))
            .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/agreements/" + agreementId + "/deliver")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DELIVERED"));

        mvc.perform(post("/api/v1/agreements/" + agreementId + "/release")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RELEASED"));
    }

    @Test
    void buyerCanDisputeNonDeliveryWhileShipped() throws Exception {
        String eventId = createEvent("Quero vinil raro", 25_000L);
        String responseId = submitProposal(eventId, supplierUserId, 12_000L);
        String agreementId = accept(eventId, responseId);

        mvc.perform(post("/api/v1/agreements/" + agreementId + "/fund")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk());
        mvc.perform(post("/api/v1/agreements/" + agreementId + "/ship")
                .header("Authorization", "Bearer " + supplierToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"trackingCode\":\"BR555\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deliveryDeadline").isNotEmpty());

        // SHIPPED is not a dead end: the buyer can dispute non-delivery.
        mvc.perform(post("/api/v1/agreements/" + agreementId + "/dispute")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Rastreio parado, nada chegou\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DISPUTED"));
    }

    @Test
    void supplierCannotFundTheEscrow() throws Exception {
        String eventId = createEvent("Quero LEGO descontinuado", 25_000L);
        String responseId = submitProposal(eventId, supplierUserId, 15_000L);
        String agreementId = accept(eventId, responseId);

        mvc.perform(post("/api/v1/agreements/" + agreementId + "/fund")
                .header("Authorization", "Bearer " + supplierToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void disputeWithinWindowCanBeResolvedOnlyByAdmin() throws Exception {
        String eventId = createEvent("Quero camisa retrô original", 25_000L);
        String responseId = submitProposal(eventId, supplierUserId, 18_000L);
        String agreementId = accept(eventId, responseId);

        mvc.perform(post("/api/v1/agreements/" + agreementId + "/fund")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk());
        mvc.perform(post("/api/v1/agreements/" + agreementId + "/ship")
                .header("Authorization", "Bearer " + supplierToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"trackingCode\":\"BR000\"}"))
            .andExpect(status().isOk());
        mvc.perform(post("/api/v1/agreements/" + agreementId + "/deliver")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk());

        mvc.perform(post("/api/v1/agreements/" + agreementId + "/dispute")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Atributo divergente do snapshot: original=false\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DISPUTED"));

        // Non-admin cannot resolve the dispute (ODR decision is admin-only).
        mvc.perform(post("/api/v1/agreements/" + agreementId + "/resolve")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"outcome\":\"REFUND\",\"note\":\"n/a\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void acceptanceAboveEscrowCeilingRollsBackTheAward() throws Exception {
        String eventId = createEvent("Quero coleção completa acima do teto", 900_000L);
        String responseId = submitProposal(eventId, supplierUserId, 500_000L);

        mvc.perform(post("/api/v1/sourcing-events/" + eventId + "/responses/" + responseId + "/accept")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isConflict());

        // The whole acceptance rolled back: the event was not awarded.
        mvc.perform(get("/api/v1/sourcing-events/" + eventId)
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void supplierCannotSubmitTwoProposalsForTheSameIntent() throws Exception {
        String eventId = createEvent("Quero gibi raro", 25_000L);
        submitProposal(eventId, supplierUserId, 10_000L);

        var offer = offerPayload(supplierUserId, 9_000L);
        mvc.perform(post("/api/v1/sourcing-events/" + eventId + "/responses")
                .header("Authorization", "Bearer " + supplierToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offer)))
            .andExpect(status().isConflict());
    }

    @Test
    void eventingProducesOutboxTrailAndInAppNotifications() throws Exception {
        String eventId = createEvent("Quero HQ edição de estreia", 25_000L);
        String responseId = submitProposal(eventId, supplierUserId, 14_000L);
        String agreementId = accept(eventId, responseId);

        mvc.perform(post("/api/v1/agreements/" + agreementId + "/fund")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk());

        // Probative trail: business changes land in the transactional outbox
        // (same transaction), with real aggregate types for routing.
        var outboxTypes = outboxEventRepository.findAll().stream()
            .map(com.marketplace.shared.infrastructure.outbox.OutboxEvent::getAggregateType)
            .collect(java.util.stream.Collectors.toSet());
        assertThat(outboxTypes).contains("SourcingEvent", "SupplierResponse", "Agreement");

        // Latency killer: the winning seller learns they won (in-app feed),
        // and funding tells them to ship.
        var supplierFeed = mvc.perform(get("/api/v1/notifications")
                .header("Authorization", "Bearer " + supplierToken))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        var types = new java.util.ArrayList<String>();
        objectMapper.readTree(supplierFeed).forEach(n -> types.add(n.get("type").asText()));
        assertThat(types).contains("proposal.accepted", "agreement.funded");

        // The recipient can mark it read; another user cannot.
        String notificationId = objectMapper.readTree(supplierFeed).get(0).get("id").asText();
        mvc.perform(post("/api/v1/notifications/" + notificationId + "/read")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/notifications/" + notificationId + "/read")
                .header("Authorization", "Bearer " + supplierToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.read").value(true));
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private String accept(String eventId, String responseId) throws Exception {
        var result = mvc.perform(post("/api/v1/sourcing-events/" + eventId + "/responses/" + responseId + "/accept")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isNoContent())
            .andReturn();
        return result.getResponse().getHeader("X-Agreement-Id");
    }

    private String createEvent(String title, long budgetCents) throws Exception {
        var createEvent = new LinkedHashMap<String, Object>();
        createEvent.put("tenantId", "tenant-default");
        createEvent.put("buyerOrganizationId", "org-1");
        createEvent.put("buyerContactName", "Comprador");
        createEvent.put("buyerContactPhone", "+5527999999999");
        createEvent.put("title", title);
        createEvent.put("description", "Detalhes");
        createEvent.put("mccCategoryCode", 5945);
        createEvent.put("productName", "Item de coleção");
        createEvent.put("productDescription", "Edição limitada");
        createEvent.put("category", "collectible");
        createEvent.put("unitOfMeasure", "UN");
        createEvent.put("quantityRequired", 1);
        createEvent.put("attributes", List.of(
            Map.of("key", "language", "type", "LANGUAGE", "value", "pt-BR")
        ));
        createEvent.put("validForHours", 24);
        createEvent.put("estimatedBudgetCents", budgetCents);

        var result = mvc.perform(post("/api/v1/sourcing-events")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createEvent)))
            .andReturn();
        if (result.getResponse().getStatus() != 201) {
            throw new AssertionError("createEvent failed: " + result.getResponse().getStatus()
                + " body=" + result.getResponse().getContentAsString()
                + " buyerToken=" + (buyerToken != null ? "present" : "null"));
        }
        var resp = result.getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    private Map<String, Object> offerPayload(String supplierId, long offerCents) {
        var offer = new LinkedHashMap<String, Object>();
        offer.put("supplierId", supplierId);
        offer.put("offerCents", offerCents);
        offer.put("leadTimeDays", 3);
        offer.put("warrantyMonths", 1);
        offer.put("condition", "USED");
        offer.put("shippingMode", "PICKUP");
        offer.put("attributes", List.of(
            Map.of("key", "language", "type", "LANGUAGE", "value", "pt-BR")
        ));
        offer.put("message", "Tenho em estoque");
        return offer;
    }

    private String submitProposal(String eventId, String supplierId, long offerCents) throws Exception {
        var resp = mvc.perform(post("/api/v1/sourcing-events/" + eventId + "/responses")
                .header("Authorization", "Bearer " + supplierToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offerPayload(supplierId, offerCents))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    private JsonNode registerOrLogin(String email, String documentNumber, String documentType, String userType) throws Exception {
        var register = new LinkedHashMap<String, Object>();
        register.put("email", email);
        register.put("password", "Strong@123");
        register.put("firstName", "Test");
        register.put("lastName", userType.toLowerCase());
        register.put("displayName", "Test " + userType);
        register.put("documentNumber", documentNumber);
        register.put("documentType", documentType);
        register.put("userType", userType);

        var registerResult = mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
            .andReturn();

        if (registerResult.getResponse().getStatus() == 201) {
            return objectMapper.readTree(registerResult.getResponse().getContentAsString());
        }

        var login = new LinkedHashMap<String, Object>();
        login.put("email", email);
        login.put("password", "Strong@123");
        var loginResp = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(loginResp);
    }
}
