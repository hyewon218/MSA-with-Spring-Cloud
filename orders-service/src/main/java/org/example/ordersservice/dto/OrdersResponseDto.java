package org.example.ordersservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.example.ordersservice.entity.Orders;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrdersResponseDto {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;

    private String orderId;

    public static OrdersResponseDto of(Orders orders) {
        return OrdersResponseDto.builder()
            .productId(orders.getProductId())
            .qty(orders.getQty())
            .unitPrice(orders.getUnitPrice())
            .totalPrice(orders.getTotalPrice())
            .orderId(orders.getOrderId())
            .build();
    }
}