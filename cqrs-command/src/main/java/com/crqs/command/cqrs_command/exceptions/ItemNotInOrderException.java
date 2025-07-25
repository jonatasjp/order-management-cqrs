package com.crqs.command.cqrs_command.exceptions;

import java.util.UUID;

public class ItemNotInOrderException extends RuntimeException {
    public ItemNotInOrderException(Long itemId, UUID correlationId) {
        super("Item ID " + itemId + " not found in order with correlation ID " + correlationId);
    }
}
