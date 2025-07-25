package com.crqs.command.cqrs_command.exceptions;

import java.util.UUID;

public class AddItemToOrderException extends RuntimeException {
    public AddItemToOrderException(UUID correlationId, Throwable cause) {
        super("Error adding item to order with correlation ID: " + correlationId, cause);
    }
}
