package com.ledok.spring.security.orderservice.service;

import com.ledok.spring.security.orderservice.advice.InvalidOrderException;
import com.ledok.spring.security.orderservice.advice.OrderNotFoundException;
import com.ledok.spring.security.orderservice.advice.TimeIncorrectException;
import com.ledok.spring.security.orderservice.controller.dto.*;
import com.ledok.spring.security.orderservice.gateway.ProductClient;
import com.ledok.spring.security.orderservice.gateway.dto.ProductDto;
import com.ledok.spring.security.orderservice.gateway.dto.ProductStockReturnDto;
import com.ledok.spring.security.orderservice.gateway.dto.ProductStockUpdateDto;
import com.ledok.spring.security.orderservice.persistence.entity.OrderEntity;
import com.ledok.spring.security.orderservice.persistence.entity.OrderItemEntity;
import com.ledok.spring.security.orderservice.persistence.entity.OrderStatus;
import com.ledok.spring.security.orderservice.persistence.repository.OrderItemRepository;
import com.ledok.spring.security.orderservice.persistence.repository.OrderRepository;
import com.ledok.spring.security.orderservice.mapper.OrderMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderItemRepository orderItemRepository;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderDto createOrder(Long userId,CreateOrderRequest request) {
        // Проверка наличия продуктов
        Map<Long, Integer> productQuantities = request.getItems().stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::getProductId,
                        OrderItemRequest::getQuantity,
                        Integer::sum));

        if (!productClient.checkProductsAvailability(productQuantities)) {
            throw new InvalidOrderException("Некоторые продукты недоступны!");
        }

        // Создание заказа
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(userId);
        orderEntity.setStatus(OrderStatus.CREATED);
        orderEntity.setCreatedAt(LocalDateTime.now());
        orderEntity.setUpdatedAt(LocalDateTime.now());

        // Расчет итоговой суммы и обновление стока
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ProductStockUpdateDto> stockUpdates = new ArrayList<>();
        Map<Long, ProductDto> productCache = new HashMap<>(); // Кеш товаров

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            ProductDto product = productCache.computeIfAbsent(productId,
                    id -> productClient.getProductById(id));

            if (product.getPrice() == null) {
                throw new InvalidOrderException("Цена продукта " + productId + " не указана");
            }

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(itemTotal);

            stockUpdates.add(new ProductStockUpdateDto(productId, quantity));
        }

        orderEntity.setTotalAmount(totalAmount);

        productClient.updateProductsStock(stockUpdates);

        if (request.getDeliveryDate() != null) {
            validateDeliveryDate(request.getDeliveryDate());
        }
        orderEntity.setDeliveryDate(request.getDeliveryDate());

        // Сохраняем заказ
        OrderEntity savedOrder = orderRepository.save(orderEntity);

        // Создание позиций заказа
        List<OrderItemEntity> items = productQuantities.entrySet().stream()
                .map(entry -> {
                    ProductDto product = productCache.get(entry.getKey());

                    OrderItemEntity orderItem = new OrderItemEntity();
                    orderItem.setOrder(savedOrder);
                    orderItem.setProductId(entry.getKey());
                    orderItem.setQuantity(entry.getValue());
                    orderItem.setPrice(product.getPrice());
                    return orderItem;
                })
                .collect(Collectors.toList());

        orderItemRepository.saveAll(items);
        savedOrder.setItems(items);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден!"));
    }

    @Override
    @Transactional
    public List<OrderDto> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден!"));

        if (orderEntity.getStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderException("Отменить можно только заказы в статусе CREATED! ");
        }

        List<ProductStockReturnDto> stockUpdates = new ArrayList<>();

        for (OrderItemEntity item : orderEntity.getItems()) {
            stockUpdates.add(new ProductStockReturnDto(
                    item.getProductId(),
                    item.getQuantity()
            ));
        }

        productClient.returnProductsStock(stockUpdates);

        orderEntity.setStatus(OrderStatus.CANCELLED);
        orderEntity.setUpdatedAt(LocalDateTime.now());
        orderEntity = orderRepository.save(orderEntity);
        return orderMapper.toDto(orderEntity);
    }

    @Override
    @Transactional
    public OrderDto updateDeliveryDate(Long orderId, DeliveryDateRequest deliveryDateRequest) {
        // Проверяем есть ли заказ
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден!"));
        // Проверяем статус заказа
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderException("Нельзя перенести отмененный или завершенный заказ");
        }

        if (deliveryDateRequest.getNewOrderDate() != null) {
            validateDeliveryDate(deliveryDateRequest.getNewOrderDate());
        }
//        LocalDateTime newDate = deliveryDateRequest.getNewOrderDate();
//        LocalTime deliveryTime = newDate.toLocalTime();
//        // Проверка диопазона времени доставки
//        if (deliveryTime.isBefore(LocalTime.of(9, 0)) || deliveryTime.isAfter(LocalTime.of(21, 0))) {
//            throw new TimeIncorrectException("Доставка возможна только с 9:00 до 21:00");
//        }
//        // Проверка диапозона даты доставки (14 дней со дня заказа)
//        LocalDateTime minDate = LocalDateTime.now().plusDays(1);
//        LocalDateTime maxDate = order.getCreatedAt().plusDays(14);
//        if (newDate.isBefore(minDate)) {
//            throw new TimeIncorrectException("Дата доставки должна быть не раньше " +
//                    minDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
//        }
//        if (newDate.isAfter(maxDate)) {
//            throw new TimeIncorrectException("Дата доставки должна быть не позже " +
//                    maxDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
//        }
        order.setStatus(OrderStatus.PROCESSING);
        order.setDeliveryDate(deliveryDateRequest.getNewOrderDate());
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public List<ProductDto> getOrderedProductsByUser(Long userId) {

        List<OrderEntity> userOrders = orderRepository.findByUserId(userId);

        Set<Long> productIds = userOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItemEntity::getProductId)
                .collect(Collectors.toSet());

        List<Long> ids = new ArrayList<>(productIds);

        return productClient.getProductsByIds(ids).getBody();
    }

    @Override
    @Transactional
    public OrderSummaryDto getOrderSummary(Long userId, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period.toLowerCase()) {
            case "день":
                startDate = endDate;
                break;
            case "неделя":
                startDate = endDate.minusWeeks(1);
                break;
            case "месяц":
                startDate = endDate.minusMonths(1);
                break;
            default:
                throw new InvalidOrderException("Неверный период. Используйте 'день', 'неделя' или 'месяц'");
        }

        // Получаем заказы за указанный период
        List<OrderEntity> orders = orderRepository.findByUserIdAndDeliveryDateBetween(
                userId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59));

        // Расчет общей суммы
        BigDecimal totalAmount = orders.stream()
                .map(OrderEntity::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderSummaryDto(startDate, endDate, totalAmount, orders.size());
    }
    private void validateDeliveryDate(LocalDateTime deliveryDate) {

        if (deliveryDate.isBefore(LocalDateTime.now())) {
            throw new TimeIncorrectException("Дата доставки не может быть в прошлом!");
        }

        LocalDateTime minDate = LocalDate.now().plusDays(1).atTime(0, 0);
        LocalDateTime maxDate = LocalDate.now().plusDays(15).atTime(0, 0);

        if (deliveryDate.isBefore(minDate) || deliveryDate.isAfter(maxDate)) {
            throw new TimeIncorrectException(
                    "Дата доставки должна быть в диапазоне 14 дней, начиная с 00:00 следующего дня!"
            );
        }

        LocalTime deliveryTime = deliveryDate.toLocalTime();

        boolean isFirstWindow = (deliveryTime.isAfter(LocalTime.of(11, 59))
                && deliveryTime.isBefore(LocalTime.of(15, 1)));

        boolean isSecondWindow = (deliveryTime.isAfter(LocalTime.of(17, 59))
                && deliveryTime.isBefore(LocalTime.of(21, 1)));

        if (!isFirstWindow && !isSecondWindow) {
            throw new TimeIncorrectException(
                    "Доставка осуществляется только в двух временных диапазонах:\n" +
                            "• с 12:00 до 15:00\n" +
                            "• с 18:00 до 21:00"
            );
        }
    }
}
