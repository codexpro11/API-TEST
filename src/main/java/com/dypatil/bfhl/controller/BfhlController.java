package com.dypatil.bfhl.controller;

import com.dypatil.bfhl.dto.BfhlRequest;
import com.dypatil.bfhl.dto.BfhlResponse;
import com.dypatil.bfhl.service.BfhlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for the BFHL API.
 * Exposes POST /bfhl endpoint for processing mixed input arrays.
 * Automatically routes large payloads (>10,000 elements) to async processing.
 */
@Slf4j
@RestController
@RequestMapping("/bfhl")
@RequiredArgsConstructor
public class BfhlController {

    private static final int ASYNC_THRESHOLD = 10_000;

    private final BfhlService bfhlService;

    /**
     * POST /bfhl
     * Accepts a mixed array of strings and returns categorized, processed data.
     *
     * @param requestId the X-Request-Id header (optional)
     * @param request   the request body containing the data array
     * @return BfhlResponse with all categorized fields
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> process(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Valid @RequestBody BfhlRequest request) {

        log.info("Received POST /bfhl: requestId={}, dataSize={}",
                requestId, request.getData() != null ? request.getData().size() : 0);

        int dataSize = request.getData() != null ? request.getData().size() : 0;

        if (dataSize > ASYNC_THRESHOLD) {
            // Large payload: process asynchronously
            String correlationId = UUID.randomUUID().toString();
            log.info("Large payload detected (size={}), processing async with correlationId={}", dataSize, correlationId);

            CompletableFuture<BfhlResponse> future = bfhlService.processAsync(request, requestId, correlationId);
            BfhlResponse response = future.join(); // wait for result (non-blocking in thread pool)
            return ResponseEntity.accepted().body(response);
        }

        // Normal payload: synchronous processing
        BfhlResponse response = bfhlService.process(request, requestId);
        return ResponseEntity.ok(response);
    }
}
