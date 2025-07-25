package com.crqs.command.cqrs_command.domain.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatedOrderEvent {

    private String correlationId;
    private String customerId;
    private String date;
    private String status;
    private String eventId;
}
