package com.crqs.command.cqrs_command.sqs;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsPublisher {

    private final SqsTemplate sqsTemplate;

    public void sendMessage(String queueName, Object payload) {
        try {
            sqsTemplate.send(queueName, payload);
            log.info("[SEND_MESSAGE][SUCCESS] - Message sent to SQS queue [{}]: {}", queueName, payload);
        } catch (Exception e) {
            log.error("[SEND_MESSAGE][ERROR] - Failed to send message to SQS queue [{}]: {}", queueName, e.getMessage(), e);
        }
    }
}
