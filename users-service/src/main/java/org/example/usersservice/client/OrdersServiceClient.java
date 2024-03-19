package org.example.usersservice.client;

import java.util.List;
import org.example.usersservice.dto.OrdersResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ORDERS-SERVICE")
public interface OrdersServiceClient {

    @GetMapping("/orders-service/{userId}/orders")
    List<OrdersResponseDto> getOrders(@PathVariable String userId);
}