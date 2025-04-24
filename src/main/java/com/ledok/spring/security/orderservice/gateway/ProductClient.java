package com.ledok.spring.security.orderservice.gateway;

import com.ledok.spring.security.orderservice.gateway.dto.ProductDto;
import com.ledok.spring.security.orderservice.gateway.dto.ProductStockReturnDto;
import com.ledok.spring.security.orderservice.gateway.dto.ProductStockUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient (name = "product-service", url = "http://localhost:8082")
public interface ProductClient {

    @PostMapping("/api/products/check-availability")
    boolean checkProductsAvailability(@RequestBody Map<Long, Integer> productQuantities);

    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable Long id);

    @PostMapping("/api/products/batch")
    ResponseEntity<List<ProductDto>> getProductsByIds(@RequestBody List<Long> ids);

    @PostMapping("/api/products/update-stock")
    void updateProductsStock(@RequestBody List<ProductStockUpdateDto> updates);

    @PostMapping("/api/products/return-stock")
    void returnProductsStock(@RequestBody List<ProductStockReturnDto> updates);
}
