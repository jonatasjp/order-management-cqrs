package com.crqs.command.cqrs_command.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CancelOrderRequest(
        @NotBlank(message = "Reason is required")
        String reason
) {}
