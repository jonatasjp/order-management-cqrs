package com.crqs.command.cqrs_command.exceptions;

import java.util.UUID;

public class ConfirmOrderException extends RuntimeException {
    public ConfirmOrderException(UUID correlationId, Throwable cause) {
        super("Failed to confirm order with correlationId: " + correlationId, cause);
    }
}
