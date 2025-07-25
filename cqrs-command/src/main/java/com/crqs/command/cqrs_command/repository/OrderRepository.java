package com.crqs.command.cqrs_command.repository;

import com.crqs.command.cqrs_command.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCorrelationId(UUID correlationId);
}
