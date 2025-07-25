package com.crqs.command.cqrs_command.domain.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CanceledOrderEvent {
    private String correlationId;
    private String status;
    private String reason;
    private String eventId;
}
