package org.example.ordersservice.dto;

import lombok.Data;
import org.example.ordersservice.entity.Orders;

@Data
public class OrdersRequestDto {

    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;

    private String orderId;
    private String userId;

    public Orders toEntity() {
        return Orders.builder()
            .productId(productId)
            .qty(qty)
            .unitPrice(unitPrice)
            .totalPrice(totalPrice)
            .orderId(orderId)
            .userId(userId)
            .build();
    }
}

