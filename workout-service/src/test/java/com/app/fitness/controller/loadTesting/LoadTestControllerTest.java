package com.app.fitness.controller.loadTesting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoadTestControllerTest {

    private final LoadTestController loadTestController = new LoadTestController();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loadTestController, "instanceId", "unknown");
        ReflectionTestUtils.setField(loadTestController, "serverPort", "8081");
        mockMvc = MockMvcBuilders.standaloneSetup(loadTestController).build();
    }

    @Test
    void handleRequest_shouldReturnExpectedPayload() throws Exception {
        mockMvc.perform(get("/api/load-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instanceId").value("unknown"))
                .andExpect(jsonPath("$.serverPort").value("8081"))
                .andExpect(jsonPath("$.message").value("Load test response from workout service"))
                .andExpect(jsonPath("$.requestId").value(1));
    }

    @Test
    void health_shouldExposeProcessedRequestCount() throws Exception {
        mockMvc.perform(get("/api/load-test"))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/load-test/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("totalRequests").asLong()).isGreaterThanOrEqualTo(1L);
    }
}
