package org.example.ordersservice.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.ordersservice.dto.OrdersRequestDto;
import org.example.ordersservice.dto.OrdersResponseDto;
import org.example.ordersservice.exception.OrderNotFoundException;
import org.example.ordersservice.repository.OrdersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepository ordersRepository;

    @Override
    @Transactional
    public OrdersResponseDto createOrder(String userId, OrdersRequestDto requestDto) {
        return OrdersResponseDto.of(ordersRepository.save(requestDto.toEntity(userId)));
    }

    /* userId 에 해당하는 모든 주문 정보 가져오기 */
    @Override
    @Transactional(readOnly = true)
    public List<OrdersResponseDto> getOrdersByUserId(String userId) {
        return this.ordersRepository.findByUserId(userId)
            .stream()
            .map(OrdersResponseDto::of)
            .collect(Collectors.toList());
    }

    /* orderId 에 해당하는 주문 정보 가져오기 */
    @Override
    @Transactional(readOnly = true)
    public OrdersResponseDto getOrderByOrderId(String orderId) {
        return OrdersResponseDto.of(this.ordersRepository.findByOrderId(orderId)
            .orElseThrow(() -> new OrderNotFoundException("일치하는 주문이 존재하지 않습니다.")));
    }
}
