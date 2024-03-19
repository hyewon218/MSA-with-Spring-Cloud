package org.example.ordersservice.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordersservice.dto.OrdersRequestDto;
import org.example.ordersservice.dto.OrdersResponseDto;
import org.example.ordersservice.messagequeue.KafkaProducer;
import org.example.ordersservice.messagequeue.OrderProducer;
import org.example.ordersservice.service.OrdersService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/orders-service")
@RestController
public class OrdersController {
    private final OrdersService ordersService;
    private final Environment environment;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    /** 애플리케이션 상태 체크 **/
    @GetMapping("/health-check")
    public String status() {
        return String.format("It's Working in User Service on PORT %s",
            environment.getProperty("local.server.port"));
    }

    /** 유저의 주문 생성 **/
    @PostMapping("/{userId}/orders")
    public ResponseEntity<OrdersResponseDto> createOrders(@PathVariable("userId") String userId,
                                                          @RequestBody @Validated OrdersRequestDto ordersRequestDto) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        /* kafka */
        ordersRequestDto.setUserId(userId);
        ordersRequestDto.setOrderId(UUID.randomUUID().toString());
        ordersRequestDto.setTotalPrice(ordersRequestDto.getQty() * ordersRequestDto.getUnitPrice());

        /* send this order to the kafka */
        kafkaProducer.send("example-catalog-topic", ordersRequestDto); // order와 catalog를 연동하기 위한 kafka producer
        orderProducer.send("orders", ordersRequestDto); // 사용자의 주문 정보를 kafka topic에 전달시키는 용도

        OrdersResponseDto ordersResponseDto = mapper.map(ordersRequestDto, OrdersResponseDto.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(ordersResponseDto);
    }

    /** 유저가 주문한 모든 주문 조회 **/
    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<OrdersResponseDto>> getOrdersByUserId(@PathVariable(name = "userId") String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(this.ordersService.getOrdersByUserId(userId));
    }
}