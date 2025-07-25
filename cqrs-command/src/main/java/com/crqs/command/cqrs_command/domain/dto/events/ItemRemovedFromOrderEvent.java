package com.crqs.command.cqrs_command.domain.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRemovedFromOrderEvent {
    private String correlationId;
    private Long productId;
    private String eventId;
}
