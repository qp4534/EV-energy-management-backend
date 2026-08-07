package com.ev_energy_management.backend.exception;

public class EmailSendCooldownException extends RuntimeException {
    public EmailSendCooldownException(String message) {
        super(message);
    }
}
