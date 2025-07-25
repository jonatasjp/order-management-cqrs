package com.crqs.command.cqrs_command.rest;

import com.crqs.command.cqrs_command.domain.dto.request.AddItemRequest;
import com.crqs.command.cqrs_command.domain.dto.request.CancelOrderRequest;
import com.crqs.command.cqrs_command.domain.dto.request.OrderRequest;
import com.crqs.command.cqrs_command.domain.dto.response.OrderResponse;
import com.crqs.command.cqrs_command.exceptions.CreateOrderException;
import com.crqs.command.cqrs_command.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class OrderRestController {

    private final OrderService orderService;


    @PostMapping("/orders")
    ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest orderRequest) throws CreateOrderException {
        OrderResponse orderResponse = orderService.createOrder(orderRequest);
        return ResponseEntity.ok().body(orderResponse);
    }

    @PostMapping("/orders/{correlationId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable UUID correlationId) throws Exception {
        orderService.confirmOrder(correlationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{correlationId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID correlationId, @RequestBody CancelOrderRequest request) throws Exception {
        orderService.cancelOrder(correlationId, request.reason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{id}/items")
    public ResponseEntity<Void> addItemToOrder(@PathVariable("id") UUID correlationId,
                                               @RequestBody @Valid AddItemRequest request) {
        orderService.addItemToOrder(correlationId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/orders/{id}/items/{productId}")
    public ResponseEntity<Void> removeItemFromOrder(@PathVariable("id") UUID correlationId,
                                                    @PathVariable("productId") Long productId) {
        orderService.removeItemFromOrder(correlationId, productId);
        return ResponseEntity.ok().build();
    }


}