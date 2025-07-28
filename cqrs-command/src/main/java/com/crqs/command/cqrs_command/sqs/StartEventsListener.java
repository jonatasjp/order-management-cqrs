package com.crqs.command.cqrs_command.sqs;

import com.crqs.command.cqrs_command.domain.entity.OutboxEvent;
import com.crqs.command.cqrs_command.domain.enums.OutboxStatus;
import com.crqs.command.cqrs_command.repository.OutboxEventRepository;
import com.crqs.command.cqrs_command.util.LoggingUtil;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@Data
public class StartEventsListener {

    private final OutboxEventRepository outboxEventRepository;
    private final SqsPublisher sqsPublisher;

    @Value(value = "${queues.outboxDispatchEvents}")
    private String queueOutboxDispatchEvents;

    @SqsListener(value = "${queues.startEventsProcessing}")
    public void listenerStartEventsProcessing(String message) {
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.logOperationStart("START_EVENTS_PROCESSING", "BATCH", message);

        List<OutboxEvent> listEventsToSendQueue = outboxEventRepository.findAllByStatusOrderByIdAsc(OutboxStatus.CREATED);
        LoggingUtil.logOperationSuccess("START_EVENTS_PROCESSING", "BATCH", "Found " + listEventsToSendQueue.size() + " events to process");

        for (OutboxEvent event : listEventsToSendQueue) {
            String correlationId = event.getAggregateId().toString();
            
            try {
                LoggingUtil.logOperationStart("ENQUEUE_EVENT", correlationId, event.getId());
                
                sqsPublisher.sendMessage(queueOutboxDispatchEvents, event.getId().toString());
                
                event.setStatus(OutboxStatus.SEND_QUEUE);
                outboxEventRepository.save(event);
                
                LoggingUtil.logOperationSuccess("ENQUEUE_EVENT", correlationId, "Event sent to queue");
                
            } catch (Exception e) {
                LoggingUtil.logOperationError("ENQUEUE_EVENT", correlationId, "Failed to send event to queue", e);
            } finally {
                LoggingUtil.clearContext();
            }
        }

        LoggingUtil.logPerformance("START_EVENTS_PROCESSING", "BATCH", startTime);
        LoggingUtil.clearContext();
    }

}
