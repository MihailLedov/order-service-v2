package com.ledok.spring.security.orderservice.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ledok.spring.security.orderservice.jpa.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryDateRequest {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime newOrderDate;
}
