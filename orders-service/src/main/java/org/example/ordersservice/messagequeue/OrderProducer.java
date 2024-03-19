package org.example.ordersservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordersservice.dto.Field;
import org.example.ordersservice.dto.KafkaOrderDto;
import org.example.ordersservice.dto.OrdersRequestDto;
import org.example.ordersservice.dto.Payload;
import org.example.ordersservice.dto.Schema;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    List<Field> fields = Arrays.asList(new Field("string", true, "order_id"),
        new Field("string", true, "user_id"),
        new Field("string", true, "product_id"),
        new Field("int32", true, "qty"),
        new Field("int32", true, "total_price"),
        new Field("int32", true, "unit_price")
    );

    Schema schema = Schema.builder()
        .type("struct")
        .fields(fields)
        .optional(false)
        .name("orders")
        .build();

    public void send(String kafkaTopic, OrdersRequestDto orderDto) {
        Payload payload = Payload.builder()
            .order_id(orderDto.getOrderId())
            .user_id(orderDto.getUserId())
            .product_id(orderDto.getProductId())
            .qty(orderDto.getQty())
            .unit_price(orderDto.getUnitPrice())
            .total_price(orderDto.getTotalPrice())
            .build();

        KafkaOrderDto kafkaOrderDto = new KafkaOrderDto(schema, payload);

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(kafkaOrderDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        kafkaTemplate.send(kafkaTopic, jsonInString);
        log.info("Order Producer send data from the Order Microservice: " + kafkaOrderDto);
        //log.info(orderDto.toString());
    }
}