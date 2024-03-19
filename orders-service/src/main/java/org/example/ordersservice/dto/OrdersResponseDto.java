package org.example.ordersservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ordersservice.entity.Orders;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrdersResponseDto {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;
    private LocalDateTime createdAt;

    private String orderId;

    public static OrdersResponseDto of(Orders orders) {
        return OrdersResponseDto.builder()
            .productId(orders.getProductId())
            .qty(orders.getQty())
            .unitPrice(orders.getUnitPrice())
            .totalPrice(orders.getTotalPrice())
            .createdAt(orders.getCreatedAt())
            .orderId(orders.getOrderId())
            .build();
    }
}