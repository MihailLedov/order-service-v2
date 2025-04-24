package com.ledok.spring.security.orderservice.gateway.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockReturnDto {

    @NotNull
    private Long productId;

    @Min(1)
    private int quantityToAdd;

}
