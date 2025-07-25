package com.crqs.command.cqrs_command.service;

import com.crqs.command.cqrs_command.domain.entity.OutboxEvent;
import com.crqs.command.cqrs_command.domain.enums.OutboxStatus;
import com.crqs.command.cqrs_command.repository.OutboxEventRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Data
@AllArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxEvent saveOutboxEvent(OutboxEvent outboxEvent) {
        return outboxEventRepository.save(outboxEvent);
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
