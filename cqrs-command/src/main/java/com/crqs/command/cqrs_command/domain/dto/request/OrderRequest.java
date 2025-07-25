package com.crqs.command.cqrs_command.domain.dto.request;

import com.crqs.command.cqrs_command.domain.entity.Order;
import com.crqs.command.cqrs_command.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;
import java.util.UUID;

public record OrderRequest(
        UUID correlationId,
        @NotBlank(message = "customerId is required") String customerId
) {

    public Order toOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setCorrelationId(Optional.ofNullable(correlationId).orElse(UUID.randomUUID()));
        order.setCustomerId(customerId);
        return order;
    }

}

