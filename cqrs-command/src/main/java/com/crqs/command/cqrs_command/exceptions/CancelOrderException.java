package com.crqs.command.cqrs_command.exceptions;

import java.util.UUID;

public class CancelOrderException extends RuntimeException {
    public CancelOrderException(UUID correlationId, String reason, Throwable cause) {
        super("Failed to cancel order with correlationId: " + correlationId + ". Reason: " + reason, cause);
    }
}
