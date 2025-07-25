package com.crqs.command.cqrs_command.configuration;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class AwsConfiguration {

    @Value("${cloud.aws.region.static}")
    private String awsRegion;

    @Value("${cloud.aws.credentials.accessKey}")
    private String awsAccessKeyId;

    @Value("${cloud.aws.credentials.secretKey}")
    private String awsSecretAccessKey;

    @Value("${cloud.aws.endpoint}")
    private String sqsEndpoint;

    @Bean
    public SqsAsyncClient sqsClient() throws Exception {

        final AwsBasicCredentials credentials = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey);

        return SqsAsyncClient.builder()
                .endpointOverride(new URI(sqsEndpoint))
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(credentials)).build();
    }

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient).build();
    }

}
