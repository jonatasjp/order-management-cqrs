package com.crqs.query.cqrs_query.repository;

import com.crqs.query.cqrs_query.domain.document.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<OrderDocument, String> {
}
