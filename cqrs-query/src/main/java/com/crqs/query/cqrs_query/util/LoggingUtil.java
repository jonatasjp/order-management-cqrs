package com.crqs.query.cqrs_query.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class LoggingUtil {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String SERVICE_NAME_KEY = "serviceName";

    public static void setCorrelationContext(String correlationId, String serviceName) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(SERVICE_NAME_KEY, serviceName);
    }

    public static void setEventContext(String correlationId, String serviceName) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(SERVICE_NAME_KEY, serviceName);
    }

    public static void clearContext() {
        MDC.clear();
    }

    public static void logOperationStart(String operation, String correlationId, Object... params) {
        setCorrelationContext(correlationId, "CQRS-QUERY");
        log.info("[{}][START] - Operation started with params: {}", operation, params);
    }

    public static void logOperationSuccess(String operation, String correlationId, Object result) {
        setCorrelationContext(correlationId, "CQRS-QUERY");
        log.info("[{}][SUCCESS] - Operation completed successfully. Result: {}", operation, result);
    }

    public static void logOperationError(String operation, String correlationId, String errorMessage, Throwable error) {
        setCorrelationContext(correlationId, "CQRS-QUERY");
        log.error("[{}][ERROR] - Operation failed. Error: {}", operation, errorMessage, error);
    }

    public static void logEventReceived(String eventType, String correlationId, Object event) {
        setEventContext(correlationId, "CQRS-QUERY");
        log.info("[EVENT][RECEIVED] - Event '{}' received: {}", eventType, event);
    }

    public static void logEventProcessed(String eventType, String correlationId, Object result) {
        setEventContext(correlationId, "CQRS-QUERY");
        log.info("[EVENT][PROCESSED] - Event '{}' processed successfully. Result: {}", eventType, result);
    }

    public static void logEventAlreadyProcessed(String eventType, String correlationId, String eventId) {
        setEventContext(correlationId, "CQRS-QUERY");
        log.warn("[EVENT][DUPLICATE] - Event '{}' already processed. EventId: {}", eventType, eventId);
    }

    public static void logEventPending(String eventType, String correlationId) {
        setEventContext(correlationId, "CQRS-QUERY");
        log.info("[EVENT][PENDING] - Event '{}' saved as pending for later processing", eventType);
    }

    public static void logBusinessValidation(String operation, String correlationId, String validationMessage) {
        setCorrelationContext(correlationId, "CQRS-QUERY");
        log.warn("[{}][VALIDATION] - Business rule validation: {}", operation, validationMessage);
    }

    public static void logDatabaseOperation(String operation, String correlationId, String entityType, Object entityId) {
        setCorrelationContext(correlationId, "CQRS-QUERY");
        log.debug("[{}][DATABASE] - {} operation on {} with ID: {}", operation, operation, entityType, entityId);
    }

    public static void logPerformance(String operation, String correlationId, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        setCorrelationContext(correlationId, "CQRS-QUERY");
        log.info("[{}][PERFORMANCE] - Operation completed in {}ms", operation, duration);
    }
} 