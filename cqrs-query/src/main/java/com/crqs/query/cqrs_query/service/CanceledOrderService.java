package com.crqs.query.cqrs_query.service;

import com.crqs.query.cqrs_query.domain.document.OrderDocument;
import com.crqs.query.cqrs_query.domain.dto.events.CanceledOrderEvent;
import com.crqs.query.cqrs_query.repository.OrderRepository;
import com.crqs.query.cqrs_query.repository.ProcessedEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.crqs.query.cqrs_query.domain.enums.EnumEventTypes.ORDER_CANCELED;

@AllArgsConstructor
@Slf4j
@Component
public class CanceledOrderService {

    private final OrderRepository orderRepository;
    private final ProcessedEventsRepository processedEventsRepository;
    private final OrderServiceHelper orderServiceHelper;

    @Transactional
    public OrderDocument canceledOrderFromEvent(CanceledOrderEvent event) throws JsonProcessingException {

        final String correlationId = event.getCorrelationId();
        final String eventId = event.getEventId();

        OrderDocument document = OrderDocument.builder()
                .correlationId(correlationId)
                .status(event.getStatus())
                .cancellationReason(event.getReason())
                .build();

        if (processedEventsRepository.existsById(eventId)) {
            log.warn("[ORDER_CANCELED_EVENT][WARNING] - event already processed. CORRELATION_ID: {}, eventId: {}", correlationId, eventId);
            return orderRepository.findById(correlationId).orElse(document);
        }

        Optional<OrderDocument> orderDocumentSavedOptional = orderRepository.findById(correlationId);
        if (orderDocumentSavedOptional.isEmpty()) {
            orderServiceHelper.savePendingEventIfNotExists(eventId, correlationId, ORDER_CANCELED.name(), event);
            log.info("[ORDER_CANCELED_EVENT][WARNING] - OrderDocument not found, save event as pending. CORRELATION_ID: {}", correlationId);
            return document;
        }

        OrderDocument orderDocumentSaved = orderDocumentSavedOptional.get();
        orderDocumentSaved.setStatus(event.getStatus());
        orderDocumentSaved.setCancellationReason(event.getReason());
        orderRepository.save(orderDocumentSaved);
        processedEventsRepository.save(orderServiceHelper.buildProcessedEventDocument(eventId, correlationId, ORDER_CANCELED.name()));
        orderServiceHelper.deletePendingOrderEventById(eventId);

        return orderDocumentSaved;
    }
}
