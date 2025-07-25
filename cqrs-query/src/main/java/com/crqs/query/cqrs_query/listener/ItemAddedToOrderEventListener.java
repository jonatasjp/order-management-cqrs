package com.crqs.query.cqrs_query.listener;

import com.crqs.query.cqrs_query.domain.dto.events.ItemAddedToOrderEvent;
import com.crqs.query.cqrs_query.service.ItemAddedToOrderService;
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
public class ItemAddedToOrderEventListener {

    private final ObjectMapper objectMapper;
    private final ItemAddedToOrderService itemAddedToOrderService;

    @KafkaListener(topics = "${kafka.topics.item-added}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenItemAddedToOrder(String payload, Acknowledgment ack) throws JsonProcessingException {
        try {
            ItemAddedToOrderEvent event = objectMapper.readValue(payload, ItemAddedToOrderEvent.class);
            log.debug("[ITEM_ADDED_TO_ORDER_EVENT] received: {}", event);
            var order = itemAddedToOrderService.addItemToOrderFromEvent(event);
            log.info("[ITEM_ADDED_TO_ORDER_EVENT] processed: {} order: {}", event, order);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[ITEM_ADDED_TO_ORDER_EVENT][ERROR] error to process event {}", payload, e);
            throw e;
        }
    }

}
