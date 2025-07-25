package com.crqs.command.cqrs_command.repository;

import com.crqs.command.cqrs_command.domain.entity.Item;
import com.crqs.command.cqrs_command.domain.entity.Order;
import com.crqs.command.cqrs_command.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    Optional<OrderItem> findByOrderAndItem(Order order, Item item);
    boolean existsByOrderIdAndItemId(Long orderId, Long itemId);

}
