package com.ledok.spring.security.orderservice.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OrderSummaryDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private int orderCount;
}