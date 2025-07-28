package com.crqs.command.cqrs_command.sqs;

import com.crqs.command.cqrs_command.util.LoggingUtil;
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
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.logOperationStart("SEND_SQS_MESSAGE", "SQS", queueName, payload);
        
        try {
            sqsTemplate.send(queueName, payload);
            LoggingUtil.logOperationSuccess("SEND_SQS_MESSAGE", "SQS", "Message sent to queue: " + queueName);
            LoggingUtil.logPerformance("SEND_SQS_MESSAGE", "SQS", startTime);
        } catch (Exception e) {
            LoggingUtil.logOperationError("SEND_SQS_MESSAGE", "SQS", "Failed to send message to SQS queue", e);
        } finally {
            LoggingUtil.clearContext();
        }
    }
}
