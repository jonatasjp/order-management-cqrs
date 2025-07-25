package com.crqs.query.cqrs_query.service;

import com.crqs.query.cqrs_query.domain.document.OrderDocument;
import com.crqs.query.cqrs_query.domain.dto.events.CreatedOrderEvent;
import com.crqs.query.cqrs_query.repository.OrderRepository;
import com.crqs.query.cqrs_query.repository.ProcessedEventsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.crqs.query.cqrs_query.domain.enums.EnumEventTypes.ORDER_CREATED;

@AllArgsConstructor
@Slf4j
@Component
public class CreateOrderService {

    private final OrderRepository orderRepository;
    private final ProcessedEventsRepository processedEventsRepository;
    private final OrderServiceHelper orderServiceHelper;

    @Transactional
    public OrderDocument createOrderFromEvent(CreatedOrderEvent event) {

        OrderDocument document = orderServiceHelper.buildOrderDocument(event);

        if (processedEventsRepository.existsById(event.getEventId())) {
            log.warn("[ORDER_CREATED_EVENT][WARNING] - event already processed. correlationId: {} eventId: {}",
                    event.getCorrelationId(), event.getEventId());
            return orderRepository.findById(document.getCorrelationId()).orElse(document);
        }

        return orderRepository
                .findById(document.getCorrelationId())
                .orElseGet(() -> {
                    log.info("[ORDER_CREATED_EVENT] Create new order to correlationId: {}", document.getCorrelationId());
                    OrderDocument orderSaved = orderRepository.save(document);
                    processedEventsRepository.save(orderServiceHelper.buildProcessedEventDocument(event.getEventId(), event.getCorrelationId(), ORDER_CREATED.name()));
                    return orderSaved;
                });

    }

}
