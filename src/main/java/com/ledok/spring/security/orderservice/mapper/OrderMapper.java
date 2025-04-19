package com.ledok.spring.security.orderservice.mapper;

import com.ledok.spring.security.orderservice.controller.dto.OrderDto;
import com.ledok.spring.security.orderservice.controller.dto.OrderItemDto;
import com.ledok.spring.security.orderservice.controller.dto.OrderSummaryDto;
import com.ledok.spring.security.orderservice.jpa.entity.OrderEntity;
import com.ledok.spring.security.orderservice.jpa.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "deliveryDate", source = "deliveryDate")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "items", source = "items", qualifiedByName = "mapItems")
    OrderDto toDto(OrderEntity orderEntity);

    @Named("mapItems")
    default List<OrderItemDto> mapItems(List<OrderItemEntity> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price")
    OrderItemDto toItemDto(OrderItemEntity orderItemEntity);

//    @Mapping(target = "startDate", source = "startDate")
//    @Mapping(target = "endDate", source = "endDate")
//    @Mapping(target = "totalAmount", source = "totalAmount")
//    @Mapping(target = "ordersCount", source = "ordersCount")
//    OrderSummaryDto toSummaryDto(LocalDateTime startDate, LocalDateTime endDate,
//                                 BigDecimal totalAmount, int ordersCount);
}