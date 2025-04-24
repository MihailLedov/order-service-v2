package com.ledok.spring.security.orderservice.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockUpdateDto {
    private Long productId;
    private int quantityToSubtract;
}
