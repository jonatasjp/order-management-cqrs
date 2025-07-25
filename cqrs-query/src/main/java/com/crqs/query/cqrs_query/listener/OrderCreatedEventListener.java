package com.crqs.query.cqrs_query.listener;

import com.crqs.query.cqrs_query.domain.document.OrderDocument;
import com.crqs.query.cqrs_query.domain.dto.events.CreatedOrderEvent;
import com.crqs.query.cqrs_query.service.CreateOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class OrderCreatedEventListener {

    private final ObjectMapper objectMapper;
    private final CreateOrderService orderService;

    @KafkaListener(topics = "${kafka.topics.order-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenOrderCreated(String payload, Acknowledgment ack) throws JsonProcessingException {
        try {
            CreatedOrderEvent event = objectMapper.readValue(payload, CreatedOrderEvent.class);
            log.debug("[ORDER_CREATED_EVENT] Received: {}", event);

            OrderDocument document = orderService.createOrderFromEvent(event);
            log.info("[ORDER_CREATED_EVENT] Processed: {} OrderCreated: {}", event, document);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[ORDER_CREATED_EVENT][ERROR] error to proccess this event {}", payload, e);
            throw e;
        }
    }

}
