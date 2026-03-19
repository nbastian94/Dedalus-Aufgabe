package com.example.dedalusaufgabe.calculation.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CalculationControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postCalculateFirstCallDoesNotContainDifference() throws Exception {
        JsonNode response = postAndRead("/calculate", """
                {"amount":"45.32"}
                """);

        assertEquals("45.32", response.get("amount").asText());
        assertFalse(response.has("difference"));
    }

    @Test
    void postCalculateSecondCallContainsDifference() throws Exception {
        postAndRead("/calculate", """
                {"amount":"45.32"}
                """);
        JsonNode secondResponse = postAndRead("/calculate", """
                {"amount":"234.23"}
                """);

        Map<String, Integer> differenceMap = toDifferenceMap(secondResponse.get("difference"));
        assertEquals(1, differenceMap.get("200.00"));
        assertEquals(-1, differenceMap.get("20.00"));
        assertEquals(1, differenceMap.get("10.00"));
        assertEquals(-1, differenceMap.get("5.00"));
        assertEquals(2, differenceMap.get("2.00"));
        assertEquals(1, differenceMap.get("0.01"));
    }

    @Test
    void getCalculationsReturnsNoContentWhenStoreIsEmpty() throws Exception {
        mockMvc.perform(get("/calculations"))
                .andExpect(status().isNoContent());
    }

    @Test
    void postCalculationsStoresValueThatCanBeReadViaGetCalculations() throws Exception {
        mockMvc.perform(post("/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount":"150.00",
                                  "breakdown":[
                                    {"denomination":"100.00","count":1},
                                    {"denomination":"50.00","count":1}
                                  ]
                                }
                                """))
                .andExpect(status().isNoContent());

        String body = mockMvc.perform(get("/calculations"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode stored = objectMapper.readTree(body);
        assertEquals("150.00", stored.get("amount").asText());
        assertEquals(2, stored.get("breakdown").size());
    }

    @Test
    void getDenominationsReturnsAllConfiguredDenominations() throws Exception {
        String body = mockMvc.perform(get("/denominations"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode denominations = objectMapper.readTree(body);
        assertEquals(14, denominations.size());
        assertEquals("200.00", denominations.get(0).asText());
        assertEquals("0.01", denominations.get(13).asText());
    }

    private JsonNode postAndRead(String url, String requestBody) throws Exception {
        String body = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body);
    }

    private Map<String, Integer> toDifferenceMap(JsonNode difference) {
        Map<String, Integer> result = new HashMap<>();
        if (difference == null || !difference.isArray()) {
            return result;
        }
        for (JsonNode entry : difference) {
            result.put(entry.get("denomination").asText(), entry.get("delta").asInt());
        }
        return result;
    }
}
