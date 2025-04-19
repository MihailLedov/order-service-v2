package com.ledok.spring.security.orderservice.service;

import com.ledok.spring.security.orderservice.controller.dto.CreateOrderRequest;
import com.ledok.spring.security.orderservice.controller.dto.DeliveryDateRequest;
import com.ledok.spring.security.orderservice.controller.dto.OrderDto;
import com.ledok.spring.security.orderservice.controller.dto.OrderSummaryDto;
import com.ledok.spring.security.orderservice.feign.dto.ProductDto;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderDto createOrder(Long userId,CreateOrderRequest request);

    OrderDto getOrderById(Long id);

    List<OrderDto> getOrdersByUserId(Long userId);

    OrderDto cancelOrder(Long orderId);

    OrderDto updateDeliveryDate(Long orderId, DeliveryDateRequest deliveryDateRequest);

    List<ProductDto> getOrderedProductsByUser(Long userId);

    OrderSummaryDto getOrderSummary(Long userId, String period);
}

