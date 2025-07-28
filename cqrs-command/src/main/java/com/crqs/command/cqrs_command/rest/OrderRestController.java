package com.crqs.command.cqrs_command.rest;

import com.crqs.command.cqrs_command.domain.dto.request.AddItemRequest;
import com.crqs.command.cqrs_command.domain.dto.request.CancelOrderRequest;
import com.crqs.command.cqrs_command.domain.dto.request.OrderRequest;
import com.crqs.command.cqrs_command.domain.dto.response.OrderResponse;
import com.crqs.command.cqrs_command.exceptions.CreateOrderException;
import com.crqs.command.cqrs_command.service.OrderService;
import com.crqs.command.cqrs_command.util.LoggingUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@Slf4j
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping("/orders")
    ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest orderRequest) throws CreateOrderException {
        long startTime = System.currentTimeMillis();

        LoggingUtil.logOperationStart("REST_CREATE_ORDER", orderRequest.correlationId().toString(), orderRequest);
        
        try {
            OrderResponse orderResponse = orderService.createOrder(orderRequest);
            LoggingUtil.logOperationSuccess("REST_CREATE_ORDER", orderResponse.getCorrelationId(), orderResponse);
            LoggingUtil.logPerformance("REST_CREATE_ORDER", orderResponse.getCorrelationId(), startTime);
            return ResponseEntity.ok().body(orderResponse);
        } catch (Exception e) {
            LoggingUtil.logOperationError("REST_CREATE_ORDER", orderRequest.correlationId().toString(), "Failed to create order", e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }

    @PostMapping("/orders/{correlationId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable UUID correlationId) throws Exception {
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.logOperationStart("REST_CONFIRM_ORDER", correlationId.toString(), correlationId);
        
        try {
            orderService.confirmOrder(correlationId);
            LoggingUtil.logOperationSuccess("REST_CONFIRM_ORDER", correlationId.toString(), "Order confirmed");
            LoggingUtil.logPerformance("REST_CONFIRM_ORDER", correlationId.toString(), startTime);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LoggingUtil.logOperationError("REST_CONFIRM_ORDER", correlationId.toString(), "Failed to confirm order", e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }

    @PostMapping("/orders/{correlationId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID correlationId, @RequestBody CancelOrderRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.logOperationStart("REST_CANCEL_ORDER", correlationId.toString(), correlationId, request.reason());
        
        try {
            orderService.cancelOrder(correlationId, request.reason());
            LoggingUtil.logOperationSuccess("REST_CANCEL_ORDER", correlationId.toString(), "Order canceled");
            LoggingUtil.logPerformance("REST_CANCEL_ORDER", correlationId.toString(), startTime);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LoggingUtil.logOperationError("REST_CANCEL_ORDER", correlationId.toString(), "Failed to cancel order", e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }

    @PostMapping("/orders/{id}/items")
    public ResponseEntity<Void> addItemToOrder(@PathVariable("id") UUID correlationId,
                                               @RequestBody @Valid AddItemRequest request) {
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.logOperationStart("REST_ADD_ITEM", correlationId.toString(), correlationId, request);
        
        try {
            orderService.addItemToOrder(correlationId, request);
            LoggingUtil.logOperationSuccess("REST_ADD_ITEM", correlationId.toString(), "Item added to order");
            LoggingUtil.logPerformance("REST_ADD_ITEM", correlationId.toString(), startTime);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LoggingUtil.logOperationError("REST_ADD_ITEM", correlationId.toString(), "Failed to add item to order", e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }

    @DeleteMapping("/orders/{id}/items/{productId}")
    public ResponseEntity<Void> removeItemFromOrder(@PathVariable("id") UUID correlationId,
                                                    @PathVariable("productId") Long productId) {
        long startTime = System.currentTimeMillis();
        
        LoggingUtil.logOperationStart("REST_REMOVE_ITEM", correlationId.toString(), correlationId, productId);
        
        try {
            orderService.removeItemFromOrder(correlationId, productId);
            LoggingUtil.logOperationSuccess("REST_REMOVE_ITEM", correlationId.toString(), "Item removed from order");
            LoggingUtil.logPerformance("REST_REMOVE_ITEM", correlationId.toString(), startTime);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LoggingUtil.logOperationError("REST_REMOVE_ITEM", correlationId.toString(), "Failed to remove item from order", e);
            throw e;
        } finally {
            LoggingUtil.clearContext();
        }
    }
}