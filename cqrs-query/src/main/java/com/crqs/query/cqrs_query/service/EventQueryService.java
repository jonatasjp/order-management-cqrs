package com.crqs.query.cqrs_query.service;

import com.crqs.query.cqrs_query.domain.document.PendingOrderEventDocument;
import com.crqs.query.cqrs_query.domain.document.ProcessedEventsDocument;
import com.crqs.query.cqrs_query.domain.dto.PendingOrderEventDTO;
import com.crqs.query.cqrs_query.domain.dto.ProcessedEventDTO;
import com.crqs.query.cqrs_query.domain.enums.EnumEventTypes;
import com.crqs.query.cqrs_query.domain.dto.events.*;
import com.crqs.query.cqrs_query.repository.PendingOrderEventRepository;
import com.crqs.query.cqrs_query.repository.ProcessedEventsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventQueryService {
    private final PendingOrderEventRepository pendingOrderEventRepository;
    private final ProcessedEventsRepository processedEventsRepository;
    private final CreateOrderService createOrderService;
    private final ConfirmOrderService confirmOrderService;
    private final CanceledOrderService canceledOrderService;
    private final ItemAddedToOrderService itemAddedToOrderService;
    private final ItemRemovedFromOrderService itemRemovedFromOrderService;
    private final ObjectMapper objectMapper;

    public List<PendingOrderEventDocument> getAllPendingEvents() {
        return pendingOrderEventRepository.findAll();
    }

    public List<ProcessedEventsDocument> getAllProcessedEvents() {
        return processedEventsRepository.findAll();
    }

    public List<PendingOrderEventDTO> getAllPendingEventsDTO() {
        return getAllPendingEvents().stream()
                .map(e -> PendingOrderEventDTO.builder()
                        .id(e.getId())
                        .correlationId(e.getCorrelationId())
                        .eventType(e.getEventType())
                        .receivedAt(e.getReceivedAt())
                        .build())
                .toList();
    }

    public List<ProcessedEventDTO> getAllProcessedEventsDTO() {
        return getAllProcessedEvents().stream()
                .map(e -> ProcessedEventDTO.builder()
                        .id(e.getId())
                        .correlationId(e.getCorrelationId())
                        .eventType(e.getEventType())
                        .processedAt(e.getProcessedAt())
                        .build())
                .toList();
    }

    public List<PendingOrderEventDocument> getPendingEventsByCorrelationId(String correlationId) {
        return pendingOrderEventRepository.findAll().stream()
                .filter(e -> correlationId.equals(e.getCorrelationId()))
                .toList();
    }

    public List<ProcessedEventsDocument> getProcessedEventsByCorrelationId(String correlationId) {
        return processedEventsRepository.findAll().stream()
                .filter(e -> correlationId.equals(e.getCorrelationId()))
                .toList();
    }

    public List<PendingOrderEventDTO> getPendingEventsByCorrelationIdDTO(String correlationId) {
        return getPendingEventsByCorrelationId(correlationId).stream()
                .map(e -> PendingOrderEventDTO.builder()
                        .id(e.getId())
                        .correlationId(e.getCorrelationId())
                        .eventType(e.getEventType())
                        .receivedAt(e.getReceivedAt())
                        .build())
                .toList();
    }

    public List<ProcessedEventDTO> getProcessedEventsByCorrelationIdDTO(String correlationId) {
        return getProcessedEventsByCorrelationId(correlationId).stream()
                .map(e -> ProcessedEventDTO.builder()
                        .id(e.getId())
                        .correlationId(e.getCorrelationId())
                        .eventType(e.getEventType())
                        .processedAt(e.getProcessedAt())
                        .build())
                .toList();
    }

    public String processAllPendingEvents() {
        List<PendingOrderEventDocument> pendings = getAllPendingEvents();
        int success = 0;
        int fail = 0;
        for (PendingOrderEventDocument pending : pendings) {
            try {
                EnumEventTypes type = EnumEventTypes.valueOf(pending.getEventType());
                switch (type) {
                    case ORDER_CREATED -> {
                        CreatedOrderEvent event = objectMapper.readValue(pending.getPayload(), CreatedOrderEvent.class);
                        createOrderService.createOrderFromEvent(event);
                    }
                    case ORDER_CONFIRMED -> {
                        ConfirmedOrderEvent event = objectMapper.readValue(pending.getPayload(), ConfirmedOrderEvent.class);
                        confirmOrderService.confirmOrderFromEvent(event);
                    }
                    case ORDER_CANCELED -> {
                        CanceledOrderEvent event = objectMapper.readValue(pending.getPayload(), CanceledOrderEvent.class);
                        canceledOrderService.canceledOrderFromEvent(event);
                    }
                    case ITEM_ADDED_TO_ORDER -> {
                        ItemAddedToOrderEvent event = objectMapper.readValue(pending.getPayload(), ItemAddedToOrderEvent.class);
                        itemAddedToOrderService.addItemToOrderFromEvent(event);
                    }
                    case ITEM_REMOVED_FROM_ORDER -> {
                        ItemRemovedFromOrderEvent event = objectMapper.readValue(pending.getPayload(), ItemRemovedFromOrderEvent.class);
                        itemRemovedFromOrderService.removeItemFromOrderFromEvent(event);
                    }
                }
                success++;
            } catch (Exception e) {
                log.error("[PENDING_EVENT][ERROR] Failed to process pending event id: {} type: {}", pending.getId(), pending.getEventType(), e);
                fail++;
            }
        }
        return "Processed: " + success + ", Failed: " + fail;
    }
} 