package org.example.ordersservice.dto;

import java.util.UUID;
import lombok.Data;
import org.example.ordersservice.entity.Orders;

@Data
public class OrdersRequestDto {

    private String productId;
    private Integer qty;
    private Integer unitPrice;

    public Orders toEntity(String userId) {
        return Orders.builder()
            .productId(productId)
            .qty(qty)
            .unitPrice(unitPrice)
            .orderId(UUID.randomUUID().toString())
            .totalPrice(getQty()*getUnitPrice())
            .userId(userId)
            .build();
    }
}

