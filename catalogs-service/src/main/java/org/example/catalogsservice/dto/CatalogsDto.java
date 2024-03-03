package org.example.catalogsservice.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class CatalogsDto implements Serializable {

    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;

    private String orderId;
    private String userId;
}
