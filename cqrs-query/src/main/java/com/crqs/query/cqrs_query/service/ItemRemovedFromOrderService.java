package com.crqs.query.cqrs_query.service;

import com.crqs.query.cqrs_query.domain.document.OrderDocument;
import com.crqs.query.cqrs_query.domain.document.OrderItemDocument;
import com.crqs.query.cqrs_query.domain.dto.events.ItemRemovedFromOrderEvent;
import com.crqs.query.cqrs_query.repository.OrderRepository;
import com.crqs.query.cqrs_query.repository.ProcessedEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.crqs.query.cqrs_query.domain.enums.EnumEventTypes.ITEM_REMOVED_FROM_ORDER;

@AllArgsConstructor
@Slf4j
@Component
public class ItemRemovedFromOrderService {

    private final OrderRepository orderRepository;
    private final ProcessedEventsRepository processedEventsRepository;
    private final OrderServiceHelper orderServiceHelper;

    @Transactional
    public OrderDocument removeItemFromOrderFromEvent(ItemRemovedFromOrderEvent event) throws JsonProcessingException {
        String correlationId = event.getCorrelationId();
        String eventId = event.getEventId();

        OrderDocument document = buildPartialOrderDocument(event);

        if (processedEventsRepository.existsById(eventId)) {
            log.warn("[ITEM_REMOVED_FROM_ORDER_EVENT][WARNING] - event already processed. CORRELATION_ID: {}, eventId: {}", correlationId, eventId);
            return orderRepository.findById(correlationId).orElse(document);
        }

        Optional<OrderDocument> orderDocumentSavedOptional = orderRepository.findById(correlationId);
        if (orderDocumentSavedOptional.isEmpty()) {
            orderServiceHelper.savePendingEventIfNotExists(eventId, correlationId, ITEM_REMOVED_FROM_ORDER.name(), event);
            log.info("[ITEM_REMOVED_FROM_ORDER_EVENT][WARNING] - OrderDocument not found, save event as pending. CORRELATION_ID: {}", correlationId);
            return document;
        }

        OrderDocument order = orderDocumentSavedOptional.get();
        OrderItemDocument item = orderServiceHelper.buildOrderItemDocument(event);

        List<OrderItemDocument> removedItems = order.getRemovedItems();
        if (removedItems == null) {
            removedItems = new ArrayList<>();
        }
        removedItems.add(item);
        order.setRemovedItems(removedItems);

        orderRepository.save(order);
        processedEventsRepository.save(orderServiceHelper.buildProcessedEventDocument(eventId, correlationId, ITEM_REMOVED_FROM_ORDER.name()));
        orderServiceHelper.deletePendingOrderEventById(eventId);

        log.info("[ITEM_REMOVED_FROM_ORDER_EVENT] Item removed from order. CORRELATION_ID: {}, productId: {}, eventId: {}", correlationId, event.getProductId(), eventId);
        return order;
    }

    private OrderDocument buildPartialOrderDocument(ItemRemovedFromOrderEvent event) {
        return OrderDocument.builder()
                .correlationId(event.getCorrelationId())
                .build();
    }
}
