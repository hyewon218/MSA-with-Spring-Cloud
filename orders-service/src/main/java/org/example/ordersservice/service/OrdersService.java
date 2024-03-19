package org.example.ordersservice.service;

import java.util.List;
import org.example.ordersservice.dto.OrdersRequestDto;
import org.example.ordersservice.dto.OrdersResponseDto;


public interface OrdersService {
    OrdersResponseDto createOrder(OrdersRequestDto orderDetails);
    List<OrdersResponseDto> getOrdersByUserId(String userId);
    OrdersResponseDto getOrderByOrderId(String orderId);
}