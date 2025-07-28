package com.crqs.query.cqrs_query.rest;

import com.crqs.query.cqrs_query.service.OrderQueryService;
import com.crqs.query.cqrs_query.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.crqs.query.cqrs_query.domain.dto.OrderItemsViewDTO;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderQueryController {
    private final OrderQueryService orderQueryService;

    @GetMapping("/{correlationId}")
    public ResponseEntity<OrderItemsViewDTO> getOrderByCorrelationId(@PathVariable String correlationId) {
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.logOperationStart("REST_GET_ORDER", correlationId, correlationId);
        
        try {
            var result = orderQueryService.getOrderItemsViewByCorrelationId(correlationId)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
            
            if (result.getStatusCode().is2xxSuccessful()) {
                LoggingUtil.logOperationSuccess("REST_GET_ORDER", correlationId, "Order found");
            } else {
                LoggingUtil.logBusinessValidation("REST_GET_ORDER", correlationId, "Order not found");
            }
            
            LoggingUtil.logPerformance("REST_GET_ORDER", correlationId, startTime);
            return result;
        } catch (Exception e) {
            LoggingUtil.logOperationError("REST_GET_ORDER", correlationId, "Failed to get order", e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }
} 