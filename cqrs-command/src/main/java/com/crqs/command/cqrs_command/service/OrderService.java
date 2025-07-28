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
        Order order = orderRequest.toOrder();
        try {

            log.debug("[CREATE_ORDER][INIT][CORRELATION_ID: {}] OrderRequest: {}", order.getCorrelationId(), orderRequest.toString());

            Order createdOrder = orderRepository.save(order);

            CreatedOrderEvent createdOrderEvent = toCreatedOrderEvent(createdOrder);
            outboxEventService.saveOutboxEvent(buildOutboxEvent(createdOrder, createdOrderEvent, EnumEventTypes.ORDER_CREATED));

            log.info("[CREATE_ORDER][FINISH][SUCCESS][CORRELATION_ID: {}] finish order Creation process ID: {}",
                    order.getCorrelationId(), createdOrder.getId());

            return toOrderResponse(createdOrder);

        } catch (Exception e) {
            log.error("[CREATE_ORDER][ERROR][CorrelationId: {}] Error creating order for Customer ID: {}. Cause: {}",
                    order.getCorrelationId(), orderRequest.customerId(), e.getMessage(), e);
            throw new CreateOrderException(order.getCorrelationId(), e);
        }
    }

    @Transactional
    public void confirmOrder(UUID correlationId) throws ConfirmOrderException {
        try {
            log.info("[CONFIRM_ORDER][INIT][CORRELATION_ID: {}] Start order confirmation", correlationId);

            Order order = orderRepository.findByCorrelationId(correlationId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + correlationId));

            order.confirm();

            BigDecimal totalOrderAmount = order.getItems().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setStatus(OrderStatus.CONFIRMED);
            order.setTotalOrderAmount(totalOrderAmount);
            order = orderRepository.save(order);

            log.info("[CONFIRM_ORDER][SAVE][SUCCESS][CORRELATION_ID: {}] Order confirmed. ID: {}, Status: {}",
                    correlationId, order.getId(), order.getStatus());

            ConfirmedOrderEvent event = toConfirmedOrderEvent(order);
            outboxEventService.saveOutboxEvent(buildOutboxEvent(order, event, EnumEventTypes.ORDER_CONFIRMED));

            log.info("[CONFIRM_ORDER][SAVE_OUTBOX][SUCCESS][CORRELATION_ID: {}] Event 'ConfirmedOrderEvent' saved", correlationId);

        } catch (InvalidOrderOperationException e) {
            log.error("[CONFIRM_ORDER][ERROR][CORRELATION_ID: {}] Attempted to confirm order without items", correlationId, e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("[CONFIRM_ORDER][ERROR][CORRELATION_ID: {}] Order not found. Cause: {}", correlationId, e.getMessage(), e);
            throw e;
        } catch (IllegalStateException e) {
            log.error("[CONFIRM_ORDER][ERROR][CORRELATION_ID: {}] Order is already confirmed. Cause: {}", correlationId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[CONFIRM_ORDER][ERROR][CORRELATION_ID: {}] Failed to confirm order. Cause: {}", correlationId, e.getMessage(), e);
            throw new ConfirmOrderException(correlationId, e);
        }
    }

    @Transactional
    public void cancelOrder(UUID correlationId, String reason) throws CancelOrderException {
        try {
            log.debug("[CANCEL_ORDER][INIT][CORRELATION_ID: {}] Start order cancellation", correlationId);

            Order order = orderRepository.findByCorrelationId(correlationId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + correlationId));

            order.cancel();
            order = orderRepository.save(order);

            log.info("[CANCEL_ORDER][SAVE][SUCCESS][CORRELATION_ID: {}] Order canceled. ID: {}, Status: {}",
                    correlationId, order.getId(), order.getStatus());

            CanceledOrderEvent event = toCanceledOrderEvent(reason, order);
            outboxEventService.saveOutboxEvent(buildOutboxEvent(order, event, EnumEventTypes.ORDER_CANCELED));

            log.info("[CANCEL_ORDER][SAVE_OUTBOX][SUCCESS][CORRELATION_ID: {}] Event 'CanceledOrderEvent' saved", correlationId);

        } catch (Exception e) {
            log.error("[CANCEL_ORDER][ERROR][CORRELATION_ID: {}] Failed to cancel order. Reason: {}. Cause: {}",
                    correlationId, reason, e.getMessage(), e);
            throw new CancelOrderException(correlationId, reason, e);
        }
    }

    @Transactional
    public void addItemToOrder(UUID correlationId, AddItemRequest request) {
        log.debug("[ADD_ITEM][INIT][CORRELATION_ID: {}] Request: {}", correlationId, request);

        try {
            Order order = orderRepository.findByCorrelationId(correlationId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + correlationId));

            if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.CANCELED) {
                throw new IllegalStateException("Cannot add items to an order that is already confirmed or canceled.");
            }

            Item item = itemRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ItemNotFoundException(request.getProductId()));

            boolean exists = orderItemRepository.existsByOrderIdAndItemId(order.getId(), item.getId());
            if (exists) {
                throw new ItemAlreadyInOrderException(request.getProductId(), correlationId);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setQuantity(request.getQuantity());
            orderItem.setPrice(item.getPrice());

            orderItemRepository.save(orderItem);

            log.info("[ADD_ITEM][SUCCESS][CORRELATION_ID: {}] Item ID {} added to order ID {}",
                    correlationId, item.getId(), order.getId());

            ItemAddedToOrderEvent event = toItemAddedToOrderEvent(correlationId, request, item);
            outboxEventService.saveOutboxEvent(buildOutboxEvent(order, event, EnumEventTypes.ITEM_ADDED_TO_ORDER));

            log.info("[ADD_ITEM][EVENT][SUCCESS][CORRELATION_ID: {}] Event 'ItemAddedToOrder' saved", correlationId);

        } catch (IllegalArgumentException | ItemNotFoundException | ItemAlreadyInOrderException e) {
            log.error("[ADD_ITEM][ERROR][CORRELATION_ID: {}] {}", correlationId, e.getMessage(), e);
            throw e;
        } catch (IllegalStateException e) {
            log.error("[ADD_ITEM][ERROR][CORRELATION_ID: {}] {} Cannot add items to an order that is already confirmed or canceled", correlationId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[ADD_ITEM][ERROR][CORRELATION_ID: {}] Error adding item. Cause: {}", correlationId, e.getMessage(), e);
            throw new AddItemToOrderException(correlationId, e);
        }
    }

    @Transactional
    public void removeItemFromOrder(UUID correlationId, Long productId) {
        log.debug("[REMOVE_ITEM][INIT][CORRELATION_ID: {}] Product ID: {}", correlationId, productId);

        try {
            Order order = orderRepository.findByCorrelationId(correlationId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + correlationId));

            if (order.isConfirmed() || order.isCanceled()) {
                throw new IllegalStateException("Cannot remove items from an order that is already confirmed or canceled.");
            }
            
            Item item = itemRepository.findById(productId)
                    .orElseThrow(() -> new ItemNotFoundException(productId));

            OrderItem orderItem = orderItemRepository.findByOrderAndItem(order, item)
                    .orElseThrow(() -> new ItemNotInOrderException(productId, correlationId));

            orderItemRepository.delete(orderItem);

            log.info("[REMOVE_ITEM][SUCCESS][CORRELATION_ID: {}] Item ID {} removed from order ID {}",
                    correlationId, productId, order.getId());

            ItemRemovedFromOrderEvent event = toItemRemovedFromOrderEvent(correlationId, productId);

            outboxEventService.saveOutboxEvent(buildOutboxEvent(order, event, EnumEventTypes.ITEM_REMOVED_FROM_ORDER));

            log.info("[REMOVE_ITEM][EVENT][SUCCESS][CORRELATION_ID: {}] Event 'ItemRemovedFromOrder' saved", correlationId);

        } catch (IllegalStateException e) {
            log.warn("[REMOVE_ITEM][ERROR][CORRELATION_ID: {}] {} Cannot remove items from an order that is already confirmed or canceled", correlationId, e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException | ItemNotFoundException | ItemNotInOrderException e) {
            log.warn("[REMOVE_ITEM][NOT_FOUND][CORRELATION_ID: {}] {}", correlationId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[REMOVE_ITEM][ERROR][CORRELATION_ID: {}] Error removing item. Cause: {}", correlationId, e.getMessage(), e);
            throw new RemoveItemFromOrderException(correlationId, productId, e);
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
