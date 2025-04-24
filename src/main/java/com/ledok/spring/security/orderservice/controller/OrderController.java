package com.ledok.spring.security.orderservice.controller;

import com.ledok.spring.security.orderservice.controller.dto.CreateOrderRequest;
import com.ledok.spring.security.orderservice.controller.dto.DeliveryDateRequest;
import com.ledok.spring.security.orderservice.controller.dto.OrderDto;
import com.ledok.spring.security.orderservice.controller.dto.OrderSummaryDto;
import com.ledok.spring.security.orderservice.gateway.dto.ProductDto;
import com.ledok.spring.security.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(Long userId,@RequestBody @Valid CreateOrderRequest request) {
        OrderDto order = orderService.createOrder(userId,request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<List<OrderDto>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    @PutMapping("/delivery/{orderId}/change")
    public ResponseEntity<OrderDto> updateOrderDeliverDate(
            @PathVariable Long orderId,
            @RequestBody DeliveryDateRequest deliveryDateRequest) {
        return ResponseEntity.ok(orderService.updateDeliveryDate(orderId, deliveryDateRequest));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getOrderedProductsByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(orderService.getOrderedProductsByUser(userId));
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<OrderSummaryDto> getOrderSummary(
            @PathVariable Long userId,
            @RequestParam String period) {
        return ResponseEntity.ok(orderService.getOrderSummary(userId, period));
    }
}
