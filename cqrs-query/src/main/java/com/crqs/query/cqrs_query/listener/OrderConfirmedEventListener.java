package com.crqs.query.cqrs_query.listener;

import com.crqs.query.cqrs_query.domain.document.OrderDocument;
import com.crqs.query.cqrs_query.domain.dto.events.ConfirmedOrderEvent;
import com.crqs.query.cqrs_query.service.ConfirmOrderService;
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
public class OrderConfirmedEventListener {

    private final ObjectMapper objectMapper;
    private final ConfirmOrderService orderService;

    @KafkaListener(topics = "${kafka.topics.order-confirmed}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenOrderConfirmed(String payload, Acknowledgment ack) throws JsonProcessingException {
        try {
            ConfirmedOrderEvent event = objectMapper.readValue(payload, ConfirmedOrderEvent.class);
            log.debug("[ORDER_CONFIRMED_EVENT] Received: {}", payload);

            OrderDocument orderDocument = orderService.confirmOrderFromEvent(event);
            log.info("[ORDER_CONFIRMED_EVENT] Processed: {} OrderConfirmed: {}", event, orderDocument);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[ORDER_CONFIRMED_EVENT][ERROR] error to proccess this event {}", payload, e);
            throw e;
        }
    }

}
