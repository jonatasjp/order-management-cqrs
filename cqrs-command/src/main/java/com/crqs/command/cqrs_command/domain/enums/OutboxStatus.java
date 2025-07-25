package com.crqs.command.cqrs_command.domain.enums;

public enum OutboxStatus {
    CREATED,
    SEND_QUEUE,
    SEND_KAFKA
}
