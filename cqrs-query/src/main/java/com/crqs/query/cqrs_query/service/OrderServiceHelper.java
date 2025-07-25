package com.crqs.query.cqrs_query.service;

import com.crqs.query.cqrs_query.domain.document.OrderDocument;
import com.crqs.query.cqrs_query.domain.document.OrderItemDocument;
import com.crqs.query.cqrs_query.domain.document.PendingOrderEventDocument;
import com.crqs.query.cqrs_query.domain.document.ProcessedEventsDocument;
import com.crqs.query.cqrs_query.domain.dto.events.CreatedOrderEvent;
import com.crqs.query.cqrs_query.domain.dto.events.ItemAddedToOrderEvent;
import com.crqs.query.cqrs_query.domain.dto.events.ItemRemovedFromOrderEvent;
import com.crqs.query.cqrs_query.repository.PendingOrderEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class OrderServiceHelper {

    private final PendingOrderEventRepository pendingOrderEventRepository;
    private final ObjectMapper objectMapper;

    public OrderDocument buildOrderDocument(CreatedOrderEvent event) {
        return OrderDocument.builder()
                .correlationId(event.getCorrelationId())
                .customerId(event.getCustomerId())
                .date(LocalDateTime.parse(event.getDate()))
                .status(event.getStatus())
                .build();
    }

    public OrderItemDocument buildOrderItemDocument(ItemAddedToOrderEvent event) {
        return OrderItemDocument.builder()
                .correlationId(event.getCorrelationId())
                .productId(event.getProductId())
                .productName(event.getProductName())
                .price(event.getPrice())
                .quantity(event.getQuantity())
                .build();
    }

    public OrderItemDocument buildOrderItemDocument(ItemRemovedFromOrderEvent event) {
        return OrderItemDocument.builder()
                .correlationId(event.getCorrelationId())
                .productId(event.getProductId())
                .build();
    }

    public ProcessedEventsDocument buildProcessedEventDocument(String eventId, String correlationId, String eventType) {
        return ProcessedEventsDocument
                .builder()
                .id(eventId)
                .correlationId(correlationId)
                .eventType(eventType)
                .processedAt(LocalDateTime.now())
                .build();
    }

    public void savePendingEventIfNotExists(String eventId, String correlationId, String eventType, Object event) throws JsonProcessingException {
        boolean alreadyPending = pendingOrderEventRepository.existsById(eventId);
        if(!alreadyPending) {
            String payload = objectMapper.writeValueAsString(event);
            PendingOrderEventDocument pendingOrderEventDocument = PendingOrderEventDocument
                    .builder()
                    .id(eventId)
                    .correlationId(correlationId)
                    .eventType(eventType)
                    .payload(payload)
                    .receivedAt(LocalDateTime.now())
                    .build();

            pendingOrderEventRepository.save(pendingOrderEventDocument);
        }
    }

    public void deletePendingOrderEventById(String eventId) {
        pendingOrderEventRepository.deleteById(eventId);
    }

}
