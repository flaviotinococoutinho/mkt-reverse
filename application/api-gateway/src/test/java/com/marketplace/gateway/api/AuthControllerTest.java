package com.marketplace.gateway.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.user.domain.valueobject.Document;
import com.marketplace.user.domain.valueobject.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void can_register_and_login() throws Exception {
        var register = new LinkedHashMap<String, Object>();
        register.put("email", "buyer1@example.com");
        register.put("password", "Strong@123");
        register.put("firstName", "Buyer");
        register.put("lastName", "One");
        register.put("displayName", "Buyer One");
        register.put("documentNumber", "39053344705"); // valid CPF
        register.put("documentType", Document.DocumentType.CPF.name());
        register.put("userType", UserType.BUYER.name());

        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.user.id").isString())
            .andExpect(jsonPath("$.user.role").value("buyer"));

        var login = new LinkedHashMap<String, Object>();
        login.put("email", "buyer1@example.com");
        login.put("password", "Strong@123");

        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.user.email").value("buyer1@example.com"));
    }
}

