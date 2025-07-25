package com.crqs.query.cqrs_query.domain.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDocument {
    private String correlationId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
