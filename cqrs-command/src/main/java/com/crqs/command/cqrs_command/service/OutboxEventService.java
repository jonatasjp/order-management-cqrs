package com.crqs.command.cqrs_command.service;

import com.crqs.command.cqrs_command.domain.entity.OutboxEvent;
import com.crqs.command.cqrs_command.domain.enums.OutboxStatus;
import com.crqs.command.cqrs_command.repository.OutboxEventRepository;
import com.crqs.command.cqrs_command.util.LoggingUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Data
@AllArgsConstructor
@Slf4j
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxEvent saveOutboxEvent(OutboxEvent outboxEvent) {
        long startTime = System.currentTimeMillis();
        String correlationId = outboxEvent.getAggregateId().toString();
        
        LoggingUtil.logOperationStart("SAVE_OUTBOX_EVENT", correlationId, outboxEvent.getEventType());
        
        try {
            LoggingUtil.logDatabaseOperation("SAVE", correlationId, "OutboxEvent", outboxEvent.getId());
            OutboxEvent savedEvent = outboxEventRepository.save(outboxEvent);
            
            LoggingUtil.logOperationSuccess("SAVE_OUTBOX_EVENT", correlationId, "OutboxEvent saved");
            LoggingUtil.logPerformance("SAVE_OUTBOX_EVENT", correlationId, startTime);
            
            return savedEvent;
        } catch (Exception e) {
            LoggingUtil.logOperationError("SAVE_OUTBOX_EVENT", correlationId, "Failed to save OutboxEvent", e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }

    public OutboxEvent parseToOutboxEvent(String aggregateType, UUID aggregateId, String eventType, Map<String, Object> payload, OutboxStatus status, LocalDateTime sentAt) {
        return OutboxEvent
                .builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .status(status)
                .sentAt(sentAt)
                .build();
    }

}
