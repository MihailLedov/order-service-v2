package com.ledok.spring.security.orderservice.persistence.repository;

import com.ledok.spring.security.orderservice.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    List<Long> findDistinctProductIdByOrderUserId(Long userId);
}
