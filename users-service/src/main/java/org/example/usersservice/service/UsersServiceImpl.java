package org.example.usersservice.service;

import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.usersservice.client.OrdersServiceClient;
import org.example.usersservice.dto.OrdersResponseDto;
import org.example.usersservice.dto.UsersRequestDto;
import org.example.usersservice.dto.UsersResponseDto;
import org.example.usersservice.entity.Users;
import org.example.usersservice.repository.UsersRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bcryptPasswordEncoder;

    //private final RestTemplate restTemplate;
    //private final Environment environment;

    private final OrdersServiceClient ordersServiceClient;


    /*
    *  email 을 가지고 사용자를 찾아오는 메소드 재정의
    **/
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users users = usersRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        return new User(users.getEmail(), users.getEncryptedPwd(),
            true, true, true, true,
            new ArrayList<>());
    }

    @Override
    @Transactional
    public UsersResponseDto createUser(UsersRequestDto requestDto) {
        requestDto.setPassword(this.bcryptPasswordEncoder.encode(requestDto.getPassword()));
        return UsersResponseDto.of(usersRepository.save(requestDto.toEntity()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsersResponseDto> getAllUsers() {
        return this.usersRepository.findAll()
            .stream()
            .map(UsersResponseDto::of)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsersResponseDto getUserByUserId(String userId) {
        Users users = usersRepository.findByUserId(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

/*      // %s => userId, userId 가 주문한 모든 orders 내역 조회
        String orderUrl = String.format(environment.getProperty("orders_service.url"), userId);
        // Using as rest template
        ResponseEntity<List<OrdersResponseDto>> orderListResponseDto =
            restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<OrdersResponseDto>>() {// 반환받을 타입에 대한 정보 (OrdersServiceImpl 의 getOrdersByUserId 반환형)
                });
        return UsersResponseDto.of(users, orderListResponseDto.getBody());*/

        // Using a feign client
        // return UsersResponseDto.of(users, ordersServiceClient.getOrders(userId));

        // Feign Exception Handling
        List<OrdersResponseDto> orderList = null;
        try {
            orderList = ordersServiceClient.getOrders(userId);
        } catch (FeignException ex) {
            log.error(ex.getMessage());
        }

        return UsersResponseDto.of(users, orderList);
    }

    @Override
    public UsersResponseDto getUserDetailsByEmail(String email) {
        return UsersResponseDto.of(this.usersRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found")));
    }
}