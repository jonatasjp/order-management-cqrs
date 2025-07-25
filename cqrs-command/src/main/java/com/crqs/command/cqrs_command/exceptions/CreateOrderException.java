package com.crqs.command.cqrs_command.exceptions;

import java.util.UUID;

public class CreateOrderException extends RuntimeException {
    public CreateOrderException(UUID correlationId, Exception exception) {
        super("Error to create order " + correlationId, exception);
    }
}
