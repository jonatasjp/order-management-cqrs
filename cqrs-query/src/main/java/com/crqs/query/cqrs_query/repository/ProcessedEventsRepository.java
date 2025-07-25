package com.crqs.query.cqrs_query.repository;

import com.crqs.query.cqrs_query.domain.document.ProcessedEventsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProcessedEventsRepository extends MongoRepository<ProcessedEventsDocument, String> {
    boolean existsByCorrelationIdAndEventType(String correlationId, String eventType);
    Optional<ProcessedEventsDocument> findByCorrelationIdAndEventType(String correlationId, String eventType);

}
