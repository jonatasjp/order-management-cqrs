package com.crqs.query.cqrs_query.service;

import com.crqs.query.cqrs_query.domain.dto.OrderItemsViewDTO;
import com.crqs.query.cqrs_query.repository.OrderRepository;
import com.crqs.query.cqrs_query.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueryService {
    private final OrderRepository orderRepository;

    public Optional<OrderItemsViewDTO> getOrderItemsViewByCorrelationId(String correlationId) {
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.logOperationStart("GET_ORDER_BY_CORRELATION_ID", correlationId, correlationId);
        
        try {
            LoggingUtil.logDatabaseOperation("FIND", correlationId, "OrderDocument", correlationId);
            var result = orderRepository.findById(correlationId)
                    .map(order -> OrderItemsViewDTO.builder()
                            .correlationId(order.getCorrelationId())
                            .customerId(order.getCustomerId())
                            .status(order.getStatus())
                            .date(order.getDate())
                            .totalOrderAmount(order.getTotalOrderAmount())
                            .cancellationReason(order.getCancellationReason())
                            .items(order.getAddedItems())
                            .removedItems(order.getRemovedItems())
                            .build());
            
            if (result.isPresent()) {
                LoggingUtil.logOperationSuccess("GET_ORDER_BY_CORRELATION_ID", correlationId, "Order found");
            } else {
                LoggingUtil.logBusinessValidation("GET_ORDER_BY_CORRELATION_ID", correlationId, "Order not found");
            }
            
            LoggingUtil.logPerformance("GET_ORDER_BY_CORRELATION_ID", correlationId, startTime);
            return result;
            
        } catch (Exception e) {
            LoggingUtil.logOperationError("GET_ORDER_BY_CORRELATION_ID", correlationId, "Failed to get order", e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }
} 