package com.crqs.command.cqrs_command.service;

import com.crqs.command.cqrs_command.domain.dto.events.*;
import com.crqs.command.cqrs_command.domain.dto.request.AddItemRequest;
import com.crqs.command.cqrs_command.domain.dto.request.OrderRequest;
import com.crqs.command.cqrs_command.domain.dto.response.OrderResponse;
import com.crqs.command.cqrs_command.domain.entity.Item;
import com.crqs.command.cqrs_command.domain.entity.Order;
import com.crqs.command.cqrs_command.domain.entity.OrderItem;
import com.crqs.command.cqrs_command.domain.entity.OutboxEvent;
import com.crqs.command.cqrs_command.domain.enums.EnumEventTypes;
import com.crqs.command.cqrs_command.domain.enums.OrderStatus;
import com.crqs.command.cqrs_command.domain.enums.OutboxStatus;
import com.crqs.command.cqrs_command.exceptions.*;
import com.crqs.command.cqrs_command.repository.ItemRepository;
import com.crqs.command.cqrs_command.repository.OrderItemRepository;
import com.crqs.command.cqrs_command.repository.OrderRepository;
import com.crqs.command.cqrs_command.util.DateUtil;
import com.crqs.command.cqrs_command.util.JsonUtil;
import com.crqs.command.cqrs_command.util.LoggingUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {

    public static final String AGGREGATE_ORDER = "ORDER";
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OutboxEventService outboxEventService;
    private final JsonUtil jsonUtil;

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) throws CreateOrderException {
        long startTime = System.currentTimeMillis();
        Order order = orderRequest.toOrder();
        String correlationId = order.getCorrelationId().toString();
        
        LoggingUtil.logOperationStart("CREATE_ORDER", correlationId, orderRequest);
        
        try {
            LoggingUtil.logDatabaseOperation("SAVE", correlationId, "Order", "NEW");
            Order createdOrder = orderRepository.save(order);

            CreatedOrderEvent createdOrderEvent = toCreatedOrderEvent(createdOrder);
            LoggingUtil.logEventSaved("ORDER_CREATED", correlationId);
            outboxEventService.saveOutboxEvent(buildOutboxEvent(createdOrder, createdOrderEvent, EnumEventTypes.ORDER_CREATED));

            OrderResponse response = toOrderResponse(createdOrder);
            LoggingUtil.logOperationSuccess("CREATE_ORDER", correlationId, response);
            LoggingUtil.logPerformance("CREATE_ORDER", correlationId, startTime);
            
            return response;

        } catch (Exception e) {
            LoggingUtil.logOperationError("CREATE_ORDER", correlationId, "Failed to create order", e);
            throw new CreateOrderException(order.getCorrelationId(), e);
        } finally {
            LoggingUtil.clearContext();
        }
    }

    @Transactional
    public void confirmOrder(UUID correlationId) throws ConfirmOrderException {
        long startTime = System.currentTimeMillis();
        String correlationIdStr = correlationId.toString();
        
        LoggingUtil.logOperationStart("CONFIRM_ORDER", correlationIdStr, correlationId);
        
        try {
            LoggingUtil.logDatabaseOperation("FIND", correlationIdStr, "Order", correlationId);
            Order order = orderRepository.findByCorrelationId(correlationId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + correlationId));

            LoggingUtil.logBusinessValidation("CONFIRM_ORDER", correlationIdStr, "Validating order can be confirmed");
            order.confirm();

            BigDecimal totalOrderAmount = order.getItems().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setStatus(OrderStatus.CONFIRMED);
            order.setTotalOrderAmount(totalOrderAmount);
            
            LoggingUtil.logDatabaseOperation("UPDATE", correlationIdStr, "Order", order.getId());
            order = orderRepository.save(order);

            LoggingUtil.logOperationSuccess("CONFIRM_ORDER", correlationIdStr, "Order confirmed with total: " + totalOrderAmount);

            ConfirmedOrderEvent event = toConfirmedOrderEvent(order);
            LoggingUtil.logEventSaved("ORDER_CONFIRMED", correlationIdStr);
            outboxEventService.saveOutboxEvent(buildOutboxEvent(order, event, EnumEventTypes.ORDER_CONFIRMED));

            LoggingUtil.logPerformance("CONFIRM_ORDER", correlationIdStr, startTime);

        } catch (InvalidOrderOperationException e) {
            LoggingUtil.logBusinessValidation("CONFIRM_ORDER", correlationIdStr, "Cannot confirm order without items");
            throw e;
        } catch (IllegalArgumentException e) {
            LoggingUtil.logOperationError("CONFIRM_ORDER", correlationIdStr, "Order not found", e);
            throw e;
        } catch (IllegalStateException e) {
            LoggingUtil.logBusinessValidation("CONFIRM_ORDER", correlationIdStr, "Order is already confirmed");
            throw e;
        } catch (Exception e) {
            LoggingUtil.logOperationError("CONFIRM_ORDER", correlationIdStr, "Failed to confirm order", e);
            throw new ConfirmOrderException(correlationId, e);
        } finally {
            LoggingUtil.clearContext();
        }
    }

    @Transactional
    public void cancelOrder(UUID correlationId, String reason) throws CancelOrderException {
        long startTime = System.currentTimeMillis();
        String correlationIdStr = correlationId.toString();
        
        LoggingUtil.logOperationStart("CANCEL_ORDER", correlationIdStr, correlationId, reason);
        
        try {
            LoggingUtil.logDatabaseOperation("FIND", correlationIdStr, "Order", correlationId);
            Order order = orderRepository.findByCorrelationId(correlationId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + correlationId));

            LoggingUtil.logBusinessValidation("CANCEL_ORDER", correlationIdStr, "Validating order can be canceled");
            order.cancel();
            
            LoggingUtil.logDatabaseOperation("UPDATE", correlationIdStr, "Order", order.getId());
            order = orderRepository.save(order);

            LoggingUtil.logOperationSuccess("CANCEL_ORDER", correlationIdStr, "Order canceled with reason: " + reason);

            CanceledOrderEvent event = toCanceledOrderEvent(reason, order);
            LoggingUtil.logEventSaved("ORDER_CANCELED", correlationIdStr);
            outboxEventService.saveOutboxEvent(buildOutboxEvent(order, event, EnumEventTypes.ORDER_CANCELED));

            LoggingUtil.logPerformance("CANCEL_ORDER", correlationIdStr, startTime);

        } catch (Exception e) {
            LoggingUtil.logOperationError("CANCEL_ORDER", correlationIdStr, "Failed to cancel order", e);
            throw new CancelOrderException(correlationId, reason, e);
        } finally {
            LoggingUtil.clearContext();
        }
    }

    @Transactional
    public void addItemToOrder(UUID correlationId, AddItemRequest request) {
        long startTime = System.currentTimeMillis();
        String correlationIdStr = correlationId.toString();
        
        LoggingUtil.logOperationStart("ADD_ITEM", correlationIdStr, request);
        
        try {
            LoggingUtil.logDatabaseOperation("FIND", correlationIdStr, "Order", correlationId);
            Order order = orderRepository.findByCorrelationId(correlationId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + correlationId));

            LoggingUtil.logBusinessValidation("ADD_ITEM", correlationIdStr, "Validating order status for item addition");
            if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.CANCELED) {
                throw new IllegalStateException("Cannot add items to an order that is already confirmed or canceled.");
            }

            LoggingUtil.logDatabaseOperation("FIND", correlationIdStr, "Item", request.getProductId());
            Item item = itemRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ItemNotFoundException(request.getProductId()));

            LoggingUtil.logDatabaseOperation("CHECK", correlationIdStr, "OrderItem", "EXISTS");
            boolean exists = orderItemRepository.existsByOrderIdAndItemId(order.getId(), item.getId());
            if (exists) {
                throw new ItemAlreadyInOrderException(request.getProductId(), correlationId);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setQuantity(request.getQuantity());
            orderItem.setPrice(item.getPrice());

            LoggingUtil.logDatabaseOperation("SAVE", correlationIdStr, "OrderItem", "NEW");
            orderItemRepository.save(orderItem);

            LoggingUtil.logOperationSuccess("ADD_ITEM", correlationIdStr, "Item " + item.getId() + " added to order " + order.getId());

            ItemAddedToOrderEvent event = toItemAddedToOrderEvent(correlationId, request, item);
            LoggingUtil.logEventSaved("ITEM_ADDED_TO_ORDER", correlationIdStr);
            outboxEventService.saveOutboxEvent(buildOutboxEvent(order, event, EnumEventTypes.ITEM_ADDED_TO_ORDER));

            LoggingUtil.logPerformance("ADD_ITEM", correlationIdStr, startTime);

        } catch (IllegalArgumentException | ItemNotFoundException | ItemAlreadyInOrderException e) {
            LoggingUtil.logOperationError("ADD_ITEM", correlationIdStr, e.getMessage(), e);
            throw e;
        } catch (IllegalStateException e) {
            LoggingUtil.logBusinessValidation("ADD_ITEM", correlationIdStr, "Cannot add items to confirmed/canceled order");
            throw e;
        } catch (Exception e) {
            LoggingUtil.logOperationError("ADD_ITEM", correlationIdStr, "Error adding item", e);
            throw new AddItemToOrderException(correlationId, e);
        } finally {
            LoggingUtil.clearContext();
        }
    }

    @Transactional
    public void removeItemFromOrder(UUID correlationId, Long productId) {
        long startTime = System.currentTimeMillis();
        String correlationIdStr = correlationId.toString();
        
        LoggingUtil.logOperationStart("REMOVE_ITEM", correlationIdStr, productId);
        
        try {
            LoggingUtil.logDatabaseOperation("FIND", correlationIdStr, "Order", correlationId);
            Order order = orderRepository.findByCorrelationId(correlationId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + correlationId));

            LoggingUtil.logBusinessValidation("REMOVE_ITEM", correlationIdStr, "Validating order status for item removal");
            if (order.isConfirmed() || order.isCanceled()) {
                throw new IllegalStateException("Cannot remove items from an order that is already confirmed or canceled.");
            }
            
            LoggingUtil.logDatabaseOperation("FIND", correlationIdStr, "Item", productId);
            Item item = itemRepository.findById(productId)
                    .orElseThrow(() -> new ItemNotFoundException(productId));

            LoggingUtil.logDatabaseOperation("FIND", correlationIdStr, "OrderItem", "BY_ORDER_AND_ITEM");
            OrderItem orderItem = orderItemRepository.findByOrderAndItem(order, item)
                    .orElseThrow(() -> new ItemNotInOrderException(productId, correlationId));

            LoggingUtil.logDatabaseOperation("DELETE", correlationIdStr, "OrderItem", orderItem.getId());
            orderItemRepository.delete(orderItem);

            LoggingUtil.logOperationSuccess("REMOVE_ITEM", correlationIdStr, "Item " + productId + " removed from order " + order.getId());

            ItemRemovedFromOrderEvent event = toItemRemovedFromOrderEvent(correlationId, productId);
            LoggingUtil.logEventSaved("ITEM_REMOVED_FROM_ORDER", correlationIdStr);
            outboxEventService.saveOutboxEvent(buildOutboxEvent(order, event, EnumEventTypes.ITEM_REMOVED_FROM_ORDER));

            LoggingUtil.logPerformance("REMOVE_ITEM", correlationIdStr, startTime);

        } catch (IllegalStateException e) {
            LoggingUtil.logBusinessValidation("REMOVE_ITEM", correlationIdStr, "Cannot remove items from confirmed/canceled order");
            throw e;
        } catch (IllegalArgumentException | ItemNotFoundException | ItemNotInOrderException e) {
            LoggingUtil.logOperationError("REMOVE_ITEM", correlationIdStr, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LoggingUtil.logOperationError("REMOVE_ITEM", correlationIdStr, "Error removing item", e);
            throw new RemoveItemFromOrderException(correlationId, productId, e);
        } finally {
            LoggingUtil.clearContext();
        }
    }

    private CreatedOrderEvent toCreatedOrderEvent(Order createdOrder) {
        return CreatedOrderEvent.builder()
                .correlationId(createdOrder.getCorrelationId().toString())
                .customerId(createdOrder.getCustomerId())
                .date(DateUtil.formatDate(createdOrder.getDate()))
                .status(createdOrder.getStatus().toString())
                .eventId(UUID.randomUUID().toString()).build();
    }

    private static ConfirmedOrderEvent toConfirmedOrderEvent(Order order) {
        return ConfirmedOrderEvent.builder()
                .correlationId(order.getCorrelationId().toString())
                .status(order.getStatus().toString())
                .totalOrderAmount(order.getTotalOrderAmount())
                .eventId(UUID.randomUUID().toString())
                .build();
    }

    private static CanceledOrderEvent toCanceledOrderEvent(String reason, Order order) {
        return CanceledOrderEvent.builder()
                .correlationId(order.getCorrelationId().toString())
                .status(order.getStatus().toString())
                .reason(reason)
                .eventId(UUID.randomUUID().toString())
                .build();
    }

    private ItemAddedToOrderEvent toItemAddedToOrderEvent(UUID correlationId, AddItemRequest request, Item item) {
        return ItemAddedToOrderEvent.builder()
                .correlationId(correlationId.toString())
                .productId(item.getId())
                .productName(item.getName())
                .price(item.getPrice())
                .quantity(request.getQuantity())
                .eventId(UUID.randomUUID().toString())
                .build();
    }

    private ItemRemovedFromOrderEvent toItemRemovedFromOrderEvent(UUID correlationId, Long productId) {
        return ItemRemovedFromOrderEvent.builder()
                .correlationId(correlationId.toString())
                .productId(productId)
                .eventId(UUID.randomUUID().toString())
                .build();
    }

    private static OrderResponse toOrderResponse(Order createdOrder) {
        return OrderResponse.builder()
                .correlationId(createdOrder.getCorrelationId().toString())
                .customerId(createdOrder.getCustomerId())
                .date(DateUtil.formatDate(createdOrder.getDate()))
                .status(createdOrder.getStatus().toString())
                .build();
    }

    private OutboxEvent buildOutboxEvent(Order order, Object event, EnumEventTypes eventType) {
        return outboxEventService.parseToOutboxEvent(
                AGGREGATE_ORDER,
                order.getCorrelationId(),
                eventType.name(),
                jsonUtil.toMap(event),
                OutboxStatus.CREATED,
                null);
    }

}
