package com.dypatil.bfhl.service;

import com.dypatil.bfhl.dto.BfhlRequest;
import com.dypatil.bfhl.dto.BfhlResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for BFHL data processing.
 * Defines the contract for processing mixed input arrays.
 */
public interface BfhlService {

    /**
     * Processes a mixed input array synchronously.
     * Suitable for small to medium payloads.
     *
     * @param request   the incoming request with data array
     * @param requestId the X-Request-Id header value
     * @return the fully populated BfhlResponse
     */
    BfhlResponse process(BfhlRequest request, String requestId);

    /**
     * Processes a large input array asynchronously.
     * Returns a CompletableFuture for large payloads (>10,000 elements).
     *
     * @param request       the incoming request with data array
     * @param requestId     the X-Request-Id header value
     * @param correlationId a unique correlation ID for tracking async processing
     * @return CompletableFuture<BfhlResponse>
     */
    CompletableFuture<BfhlResponse> processAsync(BfhlRequest request, String requestId, String correlationId);
}
