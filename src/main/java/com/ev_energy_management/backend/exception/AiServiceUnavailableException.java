package com.ev_energy_management.backend.exception;

/** Hides downstream URLs, credentials, and response bodies from public errors. */
public class AiServiceUnavailableException extends RuntimeException {
    public AiServiceUnavailableException(String message) {
        super(message);
    }

    public AiServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
