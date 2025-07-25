package com.crqs.query.cqrs_query.domain.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmedOrderEvent {
    private String correlationId;
    private String status;
    private BigDecimal totalOrderAmount;
    private String eventId;
}

