package com.crqs.query.cqrs_query.rest;

import com.crqs.query.cqrs_query.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.crqs.query.cqrs_query.domain.dto.OrderItemsViewDTO;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderQueryController {
    private final OrderQueryService orderQueryService;

    @GetMapping("/{correlationId}")
    public ResponseEntity<OrderItemsViewDTO> getOrderByCorrelationId(@PathVariable String correlationId) {
        return orderQueryService.getOrderItemsViewByCorrelationId(correlationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
} 