package com.crqs.command.cqrs_command.sqs;

import com.crqs.command.cqrs_command.domain.entity.OutboxEvent;
import com.crqs.command.cqrs_command.domain.enums.OutboxStatus;
import com.crqs.command.cqrs_command.repository.OutboxEventRepository;
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

        log.info("[START_PROCESSING_EVENT] - Init listener to send events to queue");
        List<OutboxEvent> listEventsToSendQueue = outboxEventRepository.findAllByStatusOrderByIdAsc(OutboxStatus.CREATED);

        for (OutboxEvent event : listEventsToSendQueue) {
            try {
                log.debug("[ENQUEUE_EVENT][ID: {}] - Sending to processing queue...", event.getId());
                sqsPublisher.sendMessage(queueOutboxDispatchEvents, event.getId().toString());
                event.setStatus(OutboxStatus.SEND_QUEUE);
                outboxEventRepository.save(event);
                log.debug("[ENQUEUE_EVENT][ID: {}] - Event sent to queue and status updated.", event.getId());
            } catch (Exception e) {
                log.error("[ENQUEUE_EVENT][ID: {}] - Failed to send event to queue: {}", event.getId(), e.getMessage(), e);
            }
        }

        log.info("[FINISH_PROCESSING_EVENT] - Finish listener to send events to queue");

    }

}
