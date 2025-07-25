package com.crqs.query.cqrs_query.repository;

import com.crqs.query.cqrs_query.domain.document.PendingOrderEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PendingOrderEventRepository extends MongoRepository<PendingOrderEventDocument, String> {
    boolean existsByCorrelationIdAndEventType(String correlationId, String eventType);
    void deleteByCorrelationIdAndEventType(String correlationId, String eventType);

}
