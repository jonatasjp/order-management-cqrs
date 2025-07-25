package com.crqs.query.cqrs_query.listener;

import com.crqs.query.cqrs_query.domain.document.OrderDocument;
import com.crqs.query.cqrs_query.domain.dto.events.CanceledOrderEvent;
import com.crqs.query.cqrs_query.service.CanceledOrderService;
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
        try{
            CanceledOrderEvent event = objectMapper.readValue(payload, CanceledOrderEvent.class);
            log.debug("[ORDER_CANCELED_EVENT] Recebido: {}", event);
            OrderDocument orderDocument = canceledOrderService.canceledOrderFromEvent(event);
            log.info("[ORDER_CANCELED_EVENT] Processed: {} OrderCanceled: {}", event, orderDocument);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[ORDER_CANCELED_EVENT][ERROR] error to proccess this event {}", payload, e);
            throw e;
        }

    }

}
