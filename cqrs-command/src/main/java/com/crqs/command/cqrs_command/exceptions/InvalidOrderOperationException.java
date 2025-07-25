package com.crqs.command.cqrs_command.exceptions;

import java.util.UUID;

public class InvalidOrderOperationException extends RuntimeException {
    public InvalidOrderOperationException(String message, UUID correlationId) {
        super(String.format("[%s] %s", correlationId, message));
    }
}
