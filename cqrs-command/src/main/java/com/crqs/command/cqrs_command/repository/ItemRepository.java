package com.crqs.command.cqrs_command.repository;

import com.crqs.command.cqrs_command.domain.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
