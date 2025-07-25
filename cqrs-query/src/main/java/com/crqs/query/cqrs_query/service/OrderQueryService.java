package com.crqs.query.cqrs_query.service;

import com.crqs.query.cqrs_query.domain.dto.OrderItemsViewDTO;
import com.crqs.query.cqrs_query.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;

    public Optional<OrderItemsViewDTO> getOrderItemsViewByCorrelationId(String correlationId) {
        return orderRepository.findById(correlationId)
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
    }
} 