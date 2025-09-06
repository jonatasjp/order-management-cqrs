package com.crqs.command.cqrs_command.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;

import io.awspring.cloud.autoconfigure.messaging.SqsAutoConfiguration;
import io.awspring.cloud.autoconfigure.messaging.SqsMessagingAutoConfiguration;
import io.awspring.cloud.autoconfigure.sns.SnsAutoConfiguration;

@TestConfiguration
@ImportAutoConfiguration(exclude = {
    SqsAutoConfiguration.class,
    SqsMessagingAutoConfiguration.class,
    SnsAutoConfiguration.class
})
public class DisableAwsConfig {
}
