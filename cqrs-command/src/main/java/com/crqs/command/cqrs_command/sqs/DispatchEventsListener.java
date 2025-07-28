package com.crqs.command.cqrs_command.sqs;

import com.crqs.command.cqrs_command.configuration.KafkaTopicsProperties;
import com.crqs.command.cqrs_command.domain.entity.OutboxEvent;
import com.crqs.command.cqrs_command.domain.enums.EnumEventTypes;
import com.crqs.command.cqrs_command.domain.enums.OutboxStatus;
import com.crqs.command.cqrs_command.repository.OutboxEventRepository;
import com.crqs.command.cqrs_command.util.LoggingUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Slf4j
@Data
public class DispatchEventsListener {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @SqsListener(value = "${queues.outboxDispatchEvents}")
    public void dispatchEventsListener(String outBoxEventId) throws JsonProcessingException {
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.logOperationStart("DISPATCH_EVENT", outBoxEventId, outBoxEventId);

        Optional<OutboxEvent> optionalOutboxEvent = outboxEventRepository.findById(Long.parseLong(outBoxEventId));

        if (optionalOutboxEvent.isEmpty()) {
            LoggingUtil.logOperationError("DISPATCH_EVENT", outBoxEventId, "OutboxEvent not found", new RuntimeException("OutboxEvent not found"));
            return;
        }

        OutboxEvent event = optionalOutboxEvent.get();
        String eventType = event.getEventType();
        String correlationId = event.getAggregateId().toString();

        try {
            EnumEventTypes enumEventType = EnumEventTypes.fromEventType(eventType);

            String json = objectMapper.writeValueAsString(event.getPayload());
            Object payload = objectMapper.readValue(json, enumEventType.getPayloadClass());

            String topic = resolveTopic(enumEventType);

            LoggingUtil.logDatabaseOperation("SEND", correlationId, "Kafka", topic);
            kafkaTemplate.send(topic, event.getAggregateId().toString(), payload);

            event.setStatus(OutboxStatus.SEND_KAFKA);
            event.setSentAt(LocalDateTime.now());
            outboxEventRepository.save(event);

            LoggingUtil.logEventDispatched(eventType, correlationId, topic);
            LoggingUtil.logOperationSuccess("DISPATCH_EVENT", correlationId, "Event dispatched to Kafka");
            LoggingUtil.logPerformance("DISPATCH_EVENT", correlationId, startTime);

        } catch (Exception e) {
            LoggingUtil.logOperationError("DISPATCH_EVENT", correlationId, "Failed to dispatch event", e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }

    private String resolveTopic(EnumEventTypes eventType) {
        return switch (eventType) {
            case ORDER_CREATED -> kafkaTopicsProperties.getOrderCreated();
            case ORDER_CONFIRMED -> kafkaTopicsProperties.getOrderConfirmed();
            case ORDER_CANCELED -> kafkaTopicsProperties.getOrderCanceled();
            case ITEM_ADDED_TO_ORDER -> kafkaTopicsProperties.getItemAdded();
            case ITEM_REMOVED_FROM_ORDER -> kafkaTopicsProperties.getItemRemoved();
        };
    }


}
