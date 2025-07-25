package com.crqs.command.cqrs_command.exceptions;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Could not find order " + id);
    }
}
