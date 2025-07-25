package com.crqs.command.cqrs_command.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

    private String correlationId;
    private String customerId;
    private String date;
    private String status;

}
