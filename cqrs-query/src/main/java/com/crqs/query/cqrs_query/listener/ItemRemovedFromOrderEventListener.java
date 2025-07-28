package com.crqs.query.cqrs_query.listener;

import com.crqs.query.cqrs_query.domain.dto.events.ItemRemovedFromOrderEvent;
import com.crqs.query.cqrs_query.service.ItemRemovedFromOrderService;
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
public class ItemRemovedFromOrderEventListener {

    private final ObjectMapper objectMapper;
    private final ItemRemovedFromOrderService itemRemovedFromOrderService;

    @KafkaListener(topics = "${kafka.topics.item-removed}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenItemRemovedFromOrder(String payload, Acknowledgment ack) throws JsonProcessingException {
        long startTime = System.currentTimeMillis();
        
        try {
            ItemRemovedFromOrderEvent event = objectMapper.readValue(payload, ItemRemovedFromOrderEvent.class);
            String correlationId = event.getCorrelationId();
            
            LoggingUtil.logEventReceived("ITEM_REMOVED_FROM_ORDER", correlationId, event);

            var order = itemRemovedFromOrderService.removeItemFromOrderFromEvent(event);
            LoggingUtil.logEventProcessed("ITEM_REMOVED_FROM_ORDER", correlationId, order);
            LoggingUtil.logPerformance("ITEM_REMOVED_FROM_ORDER_EVENT_PROCESSING", correlationId, startTime);
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[ITEM_REMOVED_FROM_ORDER_EVENT][ERROR] error to process event {}", payload, e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }

}
