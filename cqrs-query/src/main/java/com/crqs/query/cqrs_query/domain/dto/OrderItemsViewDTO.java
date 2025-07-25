package com.crqs.query.cqrs_query.domain.dto;

import com.crqs.query.cqrs_query.domain.document.OrderItemDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemsViewDTO {
    private String correlationId;
    private String customerId;
    private String status;
    private LocalDateTime date;
    private BigDecimal totalOrderAmount;
    private String cancellationReason;
    private List<OrderItemDocument> items;
    private List<OrderItemDocument> removedItems;
} 