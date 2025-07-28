package com.crqs.command.cqrs_command.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class LoggingUtil {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String SERVICE_NAME_KEY = "serviceName";


    private static void setCorrelationContext(String correlationId, String serviceName) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(SERVICE_NAME_KEY, serviceName);
    }

    private static void setEventContext(String correlationId, String serviceName) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(SERVICE_NAME_KEY, serviceName);
    }

    public static void clearContext() {
        MDC.clear();
    }

    public static void logOperationStart(String operation, String correlationId, Object... params) {
        setCorrelationContext(correlationId, "CQRS-COMMAND");
        log.info("[{}][START] - Operation started with params: {}", operation, params);
    }

    public static void logOperationSuccess(String operation, String correlationId, Object result) {
        setCorrelationContext(correlationId, "CQRS-COMMAND");
        log.info("[{}][SUCCESS] - Operation completed successfully. Result: {}", operation, result);
    }

    public static void logOperationError(String operation, String correlationId, String errorMessage, Throwable error) {
        setCorrelationContext(correlationId, "CQRS-COMMAND");
        log.error("[{}][ERROR] - Operation failed. Error: {}", operation, errorMessage, error);
    }

    public static void logEventSaved(String eventType, String correlationId) {
        setEventContext(correlationId, "CQRS-COMMAND");
        log.info("[EVENT][SAVED] - Event '{}' saved to outbox", eventType);
    }

    public static void logEventDispatched(String eventType, String correlationId, String topic) {
        setEventContext(correlationId, "CQRS-COMMAND");
        log.info("[EVENT][DISPATCHED] - Event '{}' dispatched to topic: {}", eventType, topic);
    }

    public static void logBusinessValidation(String operation, String correlationId, String validationMessage) {
        setCorrelationContext(correlationId, "CQRS-COMMAND");
        log.warn("[{}][VALIDATION] - Business rule validation: {}", operation, validationMessage);
    }

    public static void logDatabaseOperation(String operation, String correlationId, String entityType, Object entityId) {
        setCorrelationContext(correlationId, "CQRS-COMMAND");
        log.debug("[{}][DATABASE] - {} operation on {} with ID: {}", operation, operation, entityType, entityId);
    }

    public static void logPerformance(String operation, String correlationId, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        setCorrelationContext(correlationId, "CQRS-COMMAND");
        log.info("[{}][PERFORMANCE] - Operation completed in {}ms", operation, duration);
    }
} 