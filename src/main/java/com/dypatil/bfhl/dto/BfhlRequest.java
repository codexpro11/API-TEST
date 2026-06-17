package com.dypatil.bfhl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for the BFHL API endpoint.
 * Accepts a mixed array of strings representing numbers, alphabets,
 * special characters, and alphanumeric values.
 */
@Data
public class BfhlRequest {

    @NotNull(message = "data field is required and must not be null")
    @JsonProperty("data")
    private List<String> data;
}
