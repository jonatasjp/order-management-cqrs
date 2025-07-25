package com.crqs.command.cqrs_command.exceptions;

import java.util.UUID;

public class RemoveItemFromOrderException extends RuntimeException {
    public RemoveItemFromOrderException(UUID correlationId, Long itemId, Throwable cause) {
        super("Error removing item " + itemId + " from order with correlation ID: " + correlationId, cause);
    }
}