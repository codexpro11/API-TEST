package com.dypatil.bfhl.controller;

import com.dypatil.bfhl.dto.BfhlRequest;
import com.dypatil.bfhl.dto.BfhlResponse;
import com.dypatil.bfhl.service.BfhlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BfhlController.
 * Tests REST endpoint behavior, headers, validation, and error handling.
 */
@WebMvcTest(BfhlController.class)
@DisplayName("BfhlController Integration Tests")
class BfhlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BfhlService bfhlService;

    private BfhlResponse buildSuccessResponse(String requestId) {
        return BfhlResponse.builder()
                .isSuccess(true)
                .requestId(requestId)
                .oddNumbers(List.of("1", "7"))
                .evenNumbers(List.of("22"))
                .alphabets(List.of("A", "B"))
                .specialCharacters(List.of("$"))
                .sum("30")
                .largestNumber("22")
                .smallestNumber("1")
                .alphabetCount(2)
                .numberCount(3)
                .specialCharacterCount(1)
                .containsDuplicates(false)
                .uniqueElementCount(6)
                .processingTimeMs(15L)
                .vowelCount(1)
                .build();
    }

    @Test
    @DisplayName("POST /bfhl should return 200 OK with valid request")
    void shouldReturn200WithValidRequest() throws Exception {
        BfhlRequest req = new BfhlRequest();
        req.setData(Arrays.asList("A", "1", "22", "$", "B", "7"));

        when(bfhlService.process(any(), eq("REQ-1001")))
                .thenReturn(buildSuccessResponse("REQ-1001"));

        mockMvc.perform(post("/bfhl")
                        .header("X-Request-Id", "REQ-1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_success").value(true))
                .andExpect(jsonPath("$.request_id").value("REQ-1001"))
                .andExpect(jsonPath("$.odd_numbers").isArray())
                .andExpect(jsonPath("$.even_numbers").isArray())
                .andExpect(jsonPath("$.alphabets").isArray())
                .andExpect(jsonPath("$.sum").value("30"));
    }

    @Test
    @DisplayName("POST /bfhl should return 400 when data is null")
    void shouldReturn400WhenDataIsNull() throws Exception {
        String body = "{\"data\": null}";

        mockMvc.perform(post("/bfhl")
                        .header("X-Request-Id", "REQ-ERR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.is_success").value(false));
    }

    @Test
    @DisplayName("POST /bfhl should return 400 for malformed JSON")
    void shouldReturn400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.is_success").value(false));
    }

    @Test
    @DisplayName("POST /bfhl should work without X-Request-Id header")
    void shouldWorkWithoutRequestIdHeader() throws Exception {
        BfhlRequest req = new BfhlRequest();
        req.setData(Arrays.asList("A", "1"));

        when(bfhlService.process(any(), isNull()))
                .thenReturn(buildSuccessResponse(null));

        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_success").value(true));
    }

    @Test
    @DisplayName("POST /bfhl should return processing_time_ms")
    void shouldReturnProcessingTime() throws Exception {
        BfhlRequest req = new BfhlRequest();
        req.setData(Arrays.asList("A", "1"));

        when(bfhlService.process(any(), any()))
                .thenReturn(buildSuccessResponse("REQ-TIME"));

        mockMvc.perform(post("/bfhl")
                        .header("X-Request-Id", "REQ-TIME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processing_time_ms").isNumber());
    }
}
