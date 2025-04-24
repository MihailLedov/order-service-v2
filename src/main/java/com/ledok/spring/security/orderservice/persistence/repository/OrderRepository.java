package com.ledok.spring.security.orderservice.persistence.repository;

import com.ledok.spring.security.orderservice.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserId(Long userId);

    List<OrderEntity> findByUserIdAndDeliveryDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
