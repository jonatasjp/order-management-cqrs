package com.crqs.query.cqrs_query.service;

import com.crqs.query.cqrs_query.domain.document.OrderDocument;
import com.crqs.query.cqrs_query.domain.dto.events.ConfirmedOrderEvent;
import com.crqs.query.cqrs_query.repository.OrderRepository;
import com.crqs.query.cqrs_query.repository.ProcessedEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.crqs.query.cqrs_query.domain.enums.EnumEventTypes.ORDER_CONFIRMED;
import static com.crqs.query.cqrs_query.domain.enums.OrderStatus.CANCELED;

@AllArgsConstructor
@Slf4j
@Component
public class ConfirmOrderService {

    private final OrderRepository orderRepository;
    private final ProcessedEventsRepository processedEventsRepository;
    private final OrderServiceHelper orderServiceHelper;

    @Transactional
    public OrderDocument confirmOrderFromEvent(ConfirmedOrderEvent event) throws JsonProcessingException {

        final String correlationId = event.getCorrelationId();
        final String eventId = event.getEventId();

        OrderDocument document = OrderDocument.builder()
                .correlationId(correlationId)
                .status(event.getStatus())
                .totalOrderAmount(event.getTotalOrderAmount())
                .build();

        if (processedEventsRepository.existsById(eventId)) {
            log.warn("[ORDER_CONFIRMED_EVENT][WARNING] - event already processed. correlationId: {} eventId: {}",
                    correlationId, eventId);
            return orderRepository.findById(correlationId).orElse(document);
        }

        Optional<OrderDocument> orderDocumentSavedOptional = orderRepository.findById(correlationId);
        if (orderDocumentSavedOptional.isEmpty()) {
            orderServiceHelper.savePendingEventIfNotExists(eventId, correlationId, ORDER_CONFIRMED.name(), event);
            log.info("[ORDER_CONFIRMED_EVENT][WARNING] - OrderDocument not found, save event as pending. correlationId: {}", correlationId);
            return document;
        }

        OrderDocument orderDocumentSaved = orderDocumentSavedOptional.get();
        orderDocumentSaved.setStatus(orderDocumentSaved.getStatus().equals(CANCELED.name()) ? orderDocumentSaved.getStatus() : event.getStatus());
        orderDocumentSaved.setTotalOrderAmount(event.getTotalOrderAmount());
        orderRepository.save(orderDocumentSaved);
        processedEventsRepository.save(orderServiceHelper.buildProcessedEventDocument(eventId, correlationId, ORDER_CONFIRMED.name()));
        orderServiceHelper.deletePendingOrderEventById(eventId);

        return orderDocumentSaved;
    }



}
