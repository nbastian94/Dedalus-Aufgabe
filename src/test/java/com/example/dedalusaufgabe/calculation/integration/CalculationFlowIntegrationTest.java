package com.example.dedalusaufgabe.calculation.integration;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CalculationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void frontendToBackendSyncIsUsedForDifferenceOnNextCalculation() throws Exception {
        mockMvc.perform(post("/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount":"150.00",
                                  "breakdown":[
                                    {"denomination":"50.00","count":1},
                                    {"denomination":"20.00","count":5}
                                  ]
                                }
                                """))
                .andExpect(status().isNoContent());

        JsonNode calculateResponse = postAndRead("/calculate", """
                {"amount":"150.00"}
                """);
        Map<String, Integer> difference = toDifferenceMap(calculateResponse.get("difference"));

        assertEquals(1, difference.get("100.00"));
        assertEquals(0, difference.get("50.00"));
        assertEquals(-5, difference.get("20.00"));

        String storedBody = mockMvc.perform(get("/calculations"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode stored = objectMapper.readTree(storedBody);
        assertEquals("150.00", stored.get("amount").asText());
        assertEquals(2, stored.get("breakdown").size());
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

    private Map<String, Integer> toDifferenceMap(JsonNode differenceNode) {
        Map<String, Integer> result = new HashMap<>();
        for (JsonNode entry : differenceNode) {
            result.put(entry.get("denomination").asText(), entry.get("delta").asInt());
        }
        return result;
    }
}
