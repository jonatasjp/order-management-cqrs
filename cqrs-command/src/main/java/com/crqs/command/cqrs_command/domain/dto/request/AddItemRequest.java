package com.crqs.command.cqrs_command.domain.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddItemRequest {
    @NotNull
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
