package com.crqs.command.cqrs_command.domain.entity;

import com.crqs.command.cqrs_command.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlation_id", nullable = false, unique = true)
    private UUID correlationId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Column(name = "total_order_amount", precision = 12, scale = 2)
    private BigDecimal totalOrderAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    public void confirm() {
        validateCanConfirm();
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        validateCanCancel();
        this.status = OrderStatus.CANCELED;
    }

    private void validateCanCancel() {
        if (isCanceled()) throw new IllegalStateException("Order is already canceled.");
    }

    private void validateCanConfirm() {
        if (isConfirmed()) throw new IllegalStateException("Order is already confirmed.");
        if (hasNoItems()) throw new IllegalStateException("Cannot confirm an order with no items.");
    }

    public boolean isConfirmed() {
        return status == OrderStatus.CONFIRMED;
    }

    public boolean isCanceled() {
        return status == OrderStatus.CANCELED;
    }

    private boolean hasNoItems() {
        return items == null || items.isEmpty();
    }

}
