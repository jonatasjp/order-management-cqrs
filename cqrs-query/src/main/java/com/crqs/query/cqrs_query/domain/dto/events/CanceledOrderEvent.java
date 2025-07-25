package com.crqs.query.cqrs_query.domain.dto.events;

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
