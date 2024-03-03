package org.example.catalogsservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.example.catalogsservice.entity.Catalogs;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogsResponseDto {

    private String productId;
    private String productName;
    private Integer unitPrice;
    private Integer stock;
    private Date createdAt;

    public static CatalogsResponseDto of(Catalogs catalogs) {
        return CatalogsResponseDto.builder()
            .productId(catalogs.getProductId())
            .productName(catalogs.getProductName())
            .unitPrice(catalogs.getUnitPrice())
            .stock(catalogs.getStock())
            .createdAt(catalogs.getCreatedAt())
            .build();
    }
}