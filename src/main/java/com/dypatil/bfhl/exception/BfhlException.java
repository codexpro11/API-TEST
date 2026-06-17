package com.dypatil.bfhl.exception;

/**
 * Custom application exception for BFHL processing errors.
 */
public class BfhlException extends RuntimeException {

    public BfhlException(String message) {
        super(message);
    }

    public BfhlException(String message, Throwable cause) {
        super(message, cause);
    }
}
