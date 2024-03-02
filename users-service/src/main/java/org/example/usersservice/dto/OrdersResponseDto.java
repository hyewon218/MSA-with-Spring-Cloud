package org.example.usersservice.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrdersResponseDto {

    private String productId;
    private Integer qty;
    private Integer unitPrice; // 단가
    private Integer totalPrice;
    private Date createdAt;

    private String orderId;
}
