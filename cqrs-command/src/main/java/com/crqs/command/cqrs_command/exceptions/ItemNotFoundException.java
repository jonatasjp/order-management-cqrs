package com.crqs.command.cqrs_command.exceptions;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(Long itemId) {
        super("Item not found with ID: " + itemId);
    }
}