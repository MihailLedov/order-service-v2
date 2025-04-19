package com.ledok.spring.security.orderservice.jpa.repository;

import com.ledok.spring.security.orderservice.jpa.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    List<Long> findDistinctProductIdByOrderUserId(Long userId);
}
