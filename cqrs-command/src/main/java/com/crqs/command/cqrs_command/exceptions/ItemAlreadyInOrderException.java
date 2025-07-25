package com.crqs.command.cqrs_command.exceptions;

import java.util.UUID;

public class ItemAlreadyInOrderException extends RuntimeException {
    public ItemAlreadyInOrderException(Long itemId, UUID correlationId) {
        super("Item with ID " + itemId + " already is present in order with correlationId " + correlationId);
    }
}
