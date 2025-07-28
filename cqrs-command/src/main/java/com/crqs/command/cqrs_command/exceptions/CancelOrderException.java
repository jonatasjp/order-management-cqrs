package com.crqs.command.cqrs_command.exceptions;

import java.util.UUID;

public class CancelOrderException extends RuntimeException {
    public CancelOrderException(UUID correlationId, String reason, Throwable cause) {
        super("Failed to cancel order with CORRELATION_ID: " + correlationId + ". Reason: " + reason, cause);
    }
}
