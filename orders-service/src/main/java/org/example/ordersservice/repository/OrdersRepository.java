package org.example.ordersservice.repository;

import java.util.List;
import java.util.Optional;
import org.example.ordersservice.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByOrderId(String orderId);
    List<Orders> findByUserId(String userId);
}

