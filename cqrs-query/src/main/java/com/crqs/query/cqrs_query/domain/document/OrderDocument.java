package com.crqs.query.cqrs_query.domain.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDocument {
    @Id
    private String correlationId;
    private String customerId;
    private String status;
    private LocalDateTime date;
    private BigDecimal totalOrderAmount;
    private String cancellationReason;
    private List<OrderItemDocument> addedItems;
    private List<OrderItemDocument> removedItems;
}
