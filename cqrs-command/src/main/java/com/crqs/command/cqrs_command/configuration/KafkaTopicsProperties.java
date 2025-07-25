package com.crqs.command.cqrs_command.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class KafkaTopicsProperties {
    private String orderCreated;
    private String orderConfirmed;
    private String orderCanceled;
    private String itemAdded;
    private String itemRemoved;
}
