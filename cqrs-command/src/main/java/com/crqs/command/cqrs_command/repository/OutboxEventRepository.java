package com.crqs.command.cqrs_command.repository;

import com.crqs.command.cqrs_command.domain.entity.OutboxEvent;
import com.crqs.command.cqrs_command.domain.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findAllByStatusOrderByIdAsc(OutboxStatus status);
}
