package com.dypatil.bfhl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for the BFHL API endpoint.
 * Contains all categorized and processed data from the input array.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BfhlResponse {

    @JsonProperty("is_success")
    private boolean isSuccess;

    @JsonProperty("request_id")
    private String requestId;

    // Correlation ID for large async payloads
    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("odd_numbers")
    private List<String> oddNumbers;

    @JsonProperty("even_numbers")
    private List<String> evenNumbers;

    @JsonProperty("alphabets")
    private List<String> alphabets;

    @JsonProperty("special_characters")
    private List<String> specialCharacters;

    @JsonProperty("sum")
    private String sum;

    @JsonProperty("largest_number")
    private String largestNumber;

    @JsonProperty("smallest_number")
    private String smallestNumber;

    @JsonProperty("alphabet_count")
    private Integer alphabetCount;

    @JsonProperty("number_count")
    private Integer numberCount;

    @JsonProperty("special_character_count")
    private Integer specialCharacterCount;

    @JsonProperty("contains_duplicates")
    private Boolean containsDuplicates;

    @JsonProperty("unique_element_count")
    private Integer uniqueElementCount;

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;

    @JsonProperty("alphabet_frequency")
    private Map<String, Integer> alphabetFrequency;

    @JsonProperty("sorted_numbers")
    private List<String> sortedNumbers;

    @JsonProperty("vowel_count")
    private Integer vowelCount;

    @JsonProperty("longest_alphabetic_value")
    private String longestAlphabeticValue;

    @JsonProperty("shortest_alphabetic_value")
    private String shortestAlphabeticValue;

    @JsonProperty("summary")
    private Summary summary;

    @Data
    @Builder
    public static class Summary {
        @JsonProperty("total_elements_received")
        private int totalElementsReceived;

        @JsonProperty("valid_elements_processed")
        private int validElementsProcessed;

        @JsonProperty("invalid_elements_ignored")
        private int invalidElementsIgnored;
    }
}
