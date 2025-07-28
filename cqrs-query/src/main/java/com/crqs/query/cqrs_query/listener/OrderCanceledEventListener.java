package com.crqs.query.cqrs_query.listener;

import com.crqs.query.cqrs_query.domain.document.OrderDocument;
import com.crqs.query.cqrs_query.domain.dto.events.CanceledOrderEvent;
import com.crqs.query.cqrs_query.service.CanceledOrderService;
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
public class OrderCanceledEventListener {

    private final ObjectMapper objectMapper;
    private final CanceledOrderService canceledOrderService;


    @KafkaListener(topics = "${kafka.topics.order-canceled}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenOrderCanceled(String payload, Acknowledgment ack) throws JsonProcessingException {
        long startTime = System.currentTimeMillis();
        
        try {
            CanceledOrderEvent event = objectMapper.readValue(payload, CanceledOrderEvent.class);
            String correlationId = event.getCorrelationId();
            
            LoggingUtil.logEventReceived("ORDER_CANCELED", correlationId, event);

            OrderDocument orderDocument = canceledOrderService.canceledOrderFromEvent(event);
            LoggingUtil.logEventProcessed("ORDER_CANCELED", correlationId, orderDocument);
            LoggingUtil.logPerformance("ORDER_CANCELED_EVENT_PROCESSING", correlationId, startTime);
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[ORDER_CANCELED_EVENT][ERROR] error to process this event {}", payload, e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }

}
