package com.crqs.command.cqrs_command.domain.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemAddedToOrderEvent {
    private String correlationId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private String eventId;
}
