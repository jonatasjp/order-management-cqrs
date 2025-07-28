package com.crqs.query.cqrs_query.listener;

import com.crqs.query.cqrs_query.domain.dto.events.ItemAddedToOrderEvent;
import com.crqs.query.cqrs_query.service.ItemAddedToOrderService;
import com.crqs.query.cqrs_query.util.LoggingUtil;
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
        long startTime = System.currentTimeMillis();
        
        try {
            ItemAddedToOrderEvent event = objectMapper.readValue(payload, ItemAddedToOrderEvent.class);
            String correlationId = event.getCorrelationId();
            
            LoggingUtil.logEventReceived("ITEM_ADDED_TO_ORDER", correlationId, event);

            var order = itemAddedToOrderService.addItemToOrderFromEvent(event);
            LoggingUtil.logEventProcessed("ITEM_ADDED_TO_ORDER", correlationId, order);
            LoggingUtil.logPerformance("ITEM_ADDED_TO_ORDER_EVENT_PROCESSING", correlationId, startTime);
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[ITEM_ADDED_TO_ORDER_EVENT][ERROR] error to process event {}", payload, e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }

}
