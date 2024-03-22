# 장애 처리와 Microservice 분산 추적
각각의 서비스에서 문제가 생겼을시에 어떻게 처리해야하는지 어떠한 서비스가 문제가 생겼고 해당 서비스의 시작점, 반환 값에 대한 흐름 추적도 필요

## CircuitBreaker 와 Resilience4j의 사용
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/31979b0c-acbf-4586-8d13-f53a72513e8d" width="80%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3cb0d49b-1a4a-41f0-be00-ac5cf80501dc" width="60%"/><br>
- 다음과 같이 user-service에 사용자의 정보를 요청했으나 timeout 오류가 발생한 경우
- 로그를 확인하면 order-service에서 문제가 발생했음을 알 수 있다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/28c9915a-74b1-4df6-9bb8-5e7f96ae6f4e" width="70%"/><br>
- 위와 같이 user-service에 요청을 보내고 user-service에서 **다른 microservice를 부르는 과정에서 오류**가 발생하는 경우
- user-service에서 발생하는 오류가 아님에도 불구하고 user-service의 응답결과로 **500에러**를 만나게 된다.
- 이를 해결하기 위해서 order-service, catalog-service로 문제가 발생하는 경우에 **더 이상 요청을 전달하지 않아야 한다.**
- feign client 측에서는 임시로 문제가 발생했을경우에 에러를 대신할 수 있는 **default값** 또는 **우회할 수 있는 값**을 보여주는게 user-service에 준비가 되어 있어야 한다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4386589a-45bf-4cf9-81b3-35f56d5e45fc" width="70%"/><br>
- 따라서 order-service 또는 catalog-service에서 문제가 발생하더라도 **user-service에 문제가 없었다면 정상적인 200 반환**을 해야한다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/6eceb8b5-d2c9-4a50-9ecb-65d6b640766d" width="70%"/><br>

<br>

### CircuitBreaker
- 문제가 생긴 서비스나 함수를 더 이상 사용하지 않도록 막아주고 문제가 생긴 서비스를 재사용할 수 있는 상태로 복구가 된다고하면 이전처럼 정상적인 흐름으로 변경하는 장치
- 장애가 발생하는 서비스에 반복적인 호출이 되지 못하게 차단
- 특정 서비스가 정상적으로 동작하지 않을 경우 다른 기능으로 대체 수행 -> 장애 회피

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ae80e276-107c-43c2-9301-c29618ac8b3b" width="40%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/073cbbf3-d021-4f4d-afa3-98a0712b330e" width="40%"/><br>

- `open`: 특별한 이유에 의해서 정상적인 서비스가 불가능한 경우에는 일정한 수치 이상에 도달했을 때 (30초안에 10번의 호출 시 절반 이상 실패, ...) CircuitBreaker가 open 상태가 되어서 최종적인 마이크로서비스에 전달하지 않는 상태
- `closed`: Circuit Breaker가 닫혔다고 하는 것은 정상적으로 다른 마이크로서비스를 사용할 수 있는 상태

<br>

spring cloud 2020버전 이전에는 CircuitBreak를 사용하기 위해서 Spring Cloud Netflix Hystrix를 사용했다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0aca4703-4423-4952-82e0-5d74012102e4" width="70%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/915f6182-41ee-403f-bb1a-f4381155548b" width="60%"/><br>
- 2019년도 이후에는 hystrix가 더 이상 개발되지 않고 대체할 수 있는 라이브러리인 `Resilience4j`를 사용
- Resilience4j는 circuitbreaker, ratelimiter, bulkhead, retry, timelimiter, cache를 제공한다.

<br>

**각각의 aplication에 spring-cloud-circuitbreaker-resilience4j 추가**
#### DefaultConfiguration
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e4a053b7-9793-4d86-b25c-8ae2510b756d" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/78223178-7d63-4817-9495-4c43ec95c4b1" width="60%"/><br>


### Resilience4JConfiguration
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4a2c6952-5c93-4323-b408-ba61d8382817" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4a2c6952-5c93-4323-b408-ba61d8382817" width="60%"/><br>

- CircuitBreakerFactory를 기본값으로 사용하는것이 아닌 임의로 커스터마이징 하기위해서는 `Customizer<Resilience4JCircuitBreakerFactory>`
- `failureRateRhreshold(4)`: CircuitBreaker를 열지 결정하는 실패 확률 -> default=50 -> 현재는 100번 중 4번
- `waitDurationInOpenState(Duration.ofMillis(1000))`: CircuitBreaker를 open한 상태를 유지하는 지속 기간을 의미, 이 기간 이후에 half-open 상태 -> default: 60초
- `slidingWindowType(...)`: CircuitBreaker가 닫힐 때(정상적인 작업이 수행가능한 상태) 통화 결과를 기록하는 데 사용되는 슬라이딩 창의 유형을 구성, 카운트 기반 또는 시간 기반 -> default: 횟수 기반
- `slidingWindowSize(2)`: CircuitBreaker가 닫힐 때 호출 결과를 기록하는 데 사용되는 슬라이딩 상의 크기를 구성 -> default: 100

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/8746cda2-7211-4cf2-9815-f0d36160ad9b" width="60%"/><br>
- TimeLimiter 추가
- `timeoutDuration(...)`: 서플라이어(Order-Serive)가 어느정도 문제가 생겼을 경우 오류로 간주할지 정하는 설정, Time Limiter는 future supplier의 time limit을 정하는 API -> default: 1초

<br>

## Users Microservice에 CircuitBreaker 적용
Order Service를 기동하지 않은 상태에서 user 정보 get

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ff27905e-9b48-4dcf-906d-16b36d1a0d0e" width="70%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/31a87bed-5400-4f64-accd-0567a0c25b44" width="50%"/><br>

### UserServiceImpl
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Environment env;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Override
    public UserDto create(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        // 매퍼가 매칭시킬 수 있는 환경 설정 정보 지정
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

        userRepository.save(userEntity);

        UserDto returnUserDto = mapper.map(userEntity, UserDto.class);
        return returnUserDto;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        ...

        /* ErrorDecoder */
//        List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> orderList = circuitbreaker.run(() ->
                        orderServiceClient.getOrders(userId),
                throwable -> new ArrayList<>()
        );

        userDto.setOrders(orderList);

        return userDto;
    }
...
}
```
- throwable을 통해 요청에 실패했을 경우 반환할 값 명시

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b66f67c6-74f2-478d-ab82-5de05e06be44" width="80%"/><br>
- Order Service가 기동중이지 않고도 User 정보는 반환되고 있다.

### CircuitBreaker를 커스텀 - Resilience4jConfig
```java
@Configuration
public class Resilience4jConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(4)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(2) // 2번의 카운트가 마지막에 저장된다.
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(4))
                .build();
        
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig)
                .circuitBreakerConfig(circuitBreakerConfig)
                .build()

        );

    }
}
```

Order Service 기동 - 주문 1개 생성 후 user 정보 get<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/58cb41c2-3fb8-4e85-9388-1fc6b9cfa98b" width="70%"/><br>

Order Service 중지<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/197b12bc-acd8-4013-a7be-54f6c248f159" width="70%"/><br>

<br>

## 분산 추적의 개요 Zipkin 서버 설치
마이크로서비스가 독립적으로 자체적인 서비스가 작동하는 것이 아닌 연쇄적으로 여러개의 서비스가 실행되는 과정에서<br>
해당하는 요청 정보가 어떻게 실행되고 어느단계를 거쳐서 어느 마이크로서비스로 이동되는지 추적할 수 있는 Zipkin에 대해서 알아본다.

### Zipkin
- https://zipkin.io/
- Twitter에서 사용하는 분산 환경의 Timing 데이터 수집, 추적 시스템 (오픈소스)
- Google Drapper에서 발전하였으며, 분산환경에서의 시스템 병목 현상 파악
- Collector, Query Service, Database WebUI로 구성
- Span
  - 하나의 요청에 사용되는 작업의 단위
  - 64 bit unique ID
- Trace
  - 트리 구조로 이뤄진 span 셋
  - 하나의 요청에 대한 같은 Trace ID 발급<br>
  
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c63d3281-3715-482c-bb22-f9cd8c66813e" width="50%"/><br>
- 모든 Microservice는 Zipkin에 데이터를 전달한다.


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/8f33d42e-2f0f-4194-ac8a-26bbd9f4c891" width="50%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4230fd7e-8fb2-428d-a6c3-6cc830ed76ab" width="50%"/><br>


### Spring Cloud Sleuth
Zipkin과 연동하여 로그파일 데이터, 스트리밍 데이터값을 Zipkin에 전달시켜주는 역할

- 스프링 부트 애플리케이션을 Zipkin과 연동
- 요청 값에 따른 Trace ID, Span ID 부여
- Trace와 Span Ids를 로그에 추가 가능
  - servlet filter
  - rest template
  - scheduled actions
  - message channels
  - feign client

### Spring Cloud Sleuth + Zipkin

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/aa366e9a-86ca-4096-9918-30f61d5ec2c0" width="70%"/><br>
사용자의 요청이 시작되고 끝날 때까지 같은 Trace ID가 사용되고 그 사이에서 마이크로서비스 간의 Transaction 이 발생한다면 세부적인 Transaction을 위해 Span ID가 발급된다.

## Spring Cloud Sleuth + Zipkin을 이용한 Microservice의 분산
**Users Microservice 수정**

라이브러리 추가
- spring-cloud-starter-sleuth
- spring-cloud-starter-zipkin

#### application.yml
zipkin 서버 위치 지정
```yaml
spring:
  application:
    name: user-service
  zipkin:
    base-url: http://localhost:9411 # zipkin server 위치
    enabled: true # 작동 가능하도록
  sleuth:
    sampler: 
      probability: 1.0 # 발생된 로그를 어느정도의 빈도수를 가지고 zipkin에 전달할지 -> 현재 1.0은 전부 전달 == 100퍼센트
...
```
#### UserServiceImpl
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    ...
    @Override
    public UserDto getUserByUserId(String userId) {
        ...

        /* ErrorDecoder */
//        List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
        log.info("Before call orders microservice");
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> orderList = circuitbreaker.run(() ->
                        orderServiceClient.getOrders(userId),
                throwable -> new ArrayList<>()
        );
        log.info("After called orders microservice");

        userDto.setOrders(orderList);

        return userDto;
    }
...
}
```

**Orders Microservice 수정**
라이브러리 추가
- spring-cloud-starter-sleuth
- spring-cloud-starter-zipkin

```yaml
spring:
  application:
    name: order-service
  zipkin:
    base-url: http://127.0.0.1:9411
    enabled: true
  sleuth:
    sampler:
      probability: 1.0
```
#### OrderController
```java
@RestController
@RequestMapping("/order-service")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    ...

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId,
                                                     @RequestBody RequestOrder orderDetails) {
        log.info("Before add orders data");
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(orderDetails, OrderDto.class);
        orderDto.setUserId(userId);
        /* jpa */
        OrderDto createdOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);

        /* Send an order to the Kafka */
//        kafkaProducer.send("example-order-topic", orderDto);

        /* kafka */
//        orderDto.setOrderId(UUID.randomUUID().toString());
//        orderDto.setTotalPrice(orderDetails.getQty() * orderDetails.getUnitPrice());
//        ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);

//        kafkaProducer.send("example-catalog-topic", orderDto); // order와 catalog를 연동하기 위한 kafka producer
//        orderProducer.send("order", orderDto); // 사용자의 주문 정보를 kafka topic에 전달시키는 용도

        log.info("After added orders data");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) throws Exception {
        log.info("Before retrieve orders data");

        List<OrderEntity> orderList = orderService.getOrdersByUserId(userId);

        List<ResponseOrder> result = new ArrayList<>();
        ModelMapper mapper = new ModelMapper();
        orderList.forEach(v ->
                result.add(mapper.map(v, ResponseOrder.class))
        );
        log.info("After retrieved orders data");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/6319ad83-b69f-4717-8264-ef45ef6fe32e" width="70%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ee0d8671-6359-4925-91bf-9ce9978cf04f" width="70%"/><br>

<br>

#### order create
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ffd1b36e-129a-4d48-a7b4-07a28c1731c9" width="100%"/><br>
- [order-service,bf3e9de37aa6d463,bf3e9de37aa6d463]: [서비스명, trace ID, span ID]

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f32c2a40-277d-44d0-98b4-80f56fb67d2d" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2dbf5e95-5177-46db-8aec-994e7b1a9a40" width="100%"/><br>

<br>


#### User 정보 Get - User Service
```
2024-03-22T14:00:47.228+09:00  INFO 47290 --- [users-service] [o-auto-1-exec-3] [65fd107febbe8d29a8cbe0c39eceaece-8f4b11d97f20e1ff] o.e.u.service.UsersServiceImpl           : Before call orders microservice
2024-03-22T14:00:47.241+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] ---> GET http://ORDERS-SERVICE/orders-service/2f6ac900-510d-4207-88e5-032f31509589/orders HTTP/1.1
2024-03-22T14:00:47.242+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] ---> END HTTP (0-byte body)
2024-03-22T14:00:47.392+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] <--- HTTP/1.1 200 (150ms)
2024-03-22T14:00:47.392+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] connection: keep-alive
2024-03-22T14:00:47.392+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] content-type: application/json
2024-03-22T14:00:47.393+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] date: Fri, 22 Mar 2024 05:00:47 GMT
2024-03-22T14:00:47.393+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] keep-alive: timeout=60
2024-03-22T14:00:47.393+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] transfer-encoding: chunked
2024-03-22T14:00:47.393+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] 
2024-03-22T14:00:47.393+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] [{"productId":"CATALOG-0001","qty":10,"unitPrice":1500,"totalPrice":15000,"createdAt":"2024-03-22T14:00:09.159131","orderId":"d881a359-2ab6-4684-a8a0-10972da1d7b1"}]
2024-03-22T14:00:47.393+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-1] [65fd107febbe8d29a8cbe0c39eceaece-9c0d9eb3537fb658] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] <--- END HTTP (165-byte body)
2024-03-22T14:00:47.399+09:00  INFO 47290 --- [users-service] [o-auto-1-exec-3] [65fd107febbe8d29a8cbe0c39eceaece-8f4b11d97f20e1ff] o.e.u.service.UsersServiceImpl           : After called orders microservice
```

#### User 정보 Get - Order Service
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/25360000-c4cd-42ac-bb83-9e6773aa99e8"/><br>
- feign 클라이언트에서 order service를 호출하면서 생성한 trace ID와 order service에서 확인할 수 있는 trace ID가 동일하다. -> 같은 요청임을 확인<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f0717aeb-fcb0-43bc-a8ed-e7b97fad4ce5" width="100%"/><br>

Zipkin Dependency 확인<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ee3b4c04-6c00-49c5-84ab-17fc1568daf5" width="50%"/><br>

<br>

#### 강제 오류 발생 - OrderController
```java
@RestController
@RequestMapping("/order-service")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    ...

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) throws Exception {
        log.info("Before retrieve orders data");

        List<OrderEntity> orderList = orderService.getOrdersByUserId(userId);

        List<ResponseOrder> result = new ArrayList<>();
        ModelMapper mapper = new ModelMapper();
        orderList.forEach(v ->
                result.add(mapper.map(v, ResponseOrder.class))
        );

        try {
            Thread.sleep(1000);
            throw new Exception("장애 발생");
        }catch (InterruptedException e) {
            log.warn(e.getMessage());
        }

        log.info("After retrieved orders data");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
```

1건 주문 후 확인

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bac9b8a4-ee79-4414-9bcf-a515524f4e61" width="70%"/><br>

- 주문이 되었지만 예외를 발생시켰기 때문에 주문이 확인되지 않는다.

```
2024-03-22T14:52:09.431+09:00  INFO 47290 --- [users-service] [o-auto-1-exec-1] [65fd1c89c53e5dbcfdafb97bb8b5c425-8cc967b342ef6b01] o.e.u.service.UsersServiceImpl           : Before call orders microservice
2024-03-22T14:52:09.432+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-5] [65fd1c89c53e5dbcfdafb97bb8b5c425-b71981281cf2d001] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] ---> GET http://ORDERS-SERVICE/orders-service/2f6ac900-510d-4207-88e5-032f31509589/orders HTTP/1.1
2024-03-22T14:52:09.432+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-5] [65fd1c89c53e5dbcfdafb97bb8b5c425-b71981281cf2d001] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] ---> END HTTP (0-byte body)
2024-03-22T14:52:10.648+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-5] [65fd1c89c53e5dbcfdafb97bb8b5c425-b71981281cf2d001] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] <--- HTTP/1.1 500 (1215ms)
2024-03-22T14:52:10.648+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-5] [65fd1c89c53e5dbcfdafb97bb8b5c425-b71981281cf2d001] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] connection: close
2024-03-22T14:52:10.648+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-5] [65fd1c89c53e5dbcfdafb97bb8b5c425-b71981281cf2d001] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] content-type: application/json
2024-03-22T14:52:10.648+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-5] [65fd1c89c53e5dbcfdafb97bb8b5c425-b71981281cf2d001] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] date: Fri, 22 Mar 2024 05:52:10 GMT
2024-03-22T14:52:10.648+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-5] [65fd1c89c53e5dbcfdafb97bb8b5c425-b71981281cf2d001] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] transfer-encoding: chunked
2024-03-22T14:52:10.648+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-5] [65fd1c89c53e5dbcfdafb97bb8b5c425-b71981281cf2d001] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] 
2024-03-22T14:52:10.648+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-5] [65fd1c89c53e5dbcfdafb97bb8b5c425-b71981281cf2d001] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] {"timestamp":"2024-03-22T05:52:10.632+00:00","status":500,"error":"Internal Server Error","trace":"java.lang.Exception: 장애 발생\n\tat org.example.ordersservice.controller.OrdersController.getOrdersByUserId(OrdersController.java:76)\n\tat java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)\n\tat java.base/java.lang.reflect.Method.invoke(Method.java:580)\n\tat org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:259)\n\tat org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:192)\n\tat org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118)\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:920)\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:830)\n\tat org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)\n\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089)\n\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979)\n\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)\n\tat org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903)\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564)\n\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885)\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:205)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\n\tat org.springframework.web.filter.ServerHttpObservationFilter.doFilterInternal(ServerHttpObservationFilter.java:109)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167)\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90)\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482)\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344)\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:391)\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:896)\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1744)\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63)\n\tat java.base/java.lang.Thread.run(Thread.java:1583)\n","message":"장애 발생","path":"/orders-service/2f6ac900-510d-4207-88e5-032f31509589/orders"}
2024-03-22T14:52:10.648+09:00 DEBUG 47290 --- [users-service] [pool-5-thread-5] [65fd1c89c53e5dbcfdafb97bb8b5c425-b71981281cf2d001] o.e.u.client.OrdersServiceClient         : [OrdersServiceClient#getOrders] <--- END HTTP (5389-byte body)
2024-03-22T14:52:10.663+09:00  INFO 47290 --- [users-service] [o-auto-1-exec-1] [65fd1c89c53e5dbcfdafb97bb8b5c425-8cc967b342ef6b01] o.e.u.service.UsersServiceImpl           : After called orders microservice
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/60365d35-47f9-42ec-8415-96692469cae1" width="100%"/><br>

Zipkin 확인<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/fd3e9a03-2a33-49c7-9ca0-3b57f6eee037" width="100%"/><br>

Zipkin Dependency 확인<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ca3eb904-e2dc-4941-bc7a-326754b34231" width="50%"/><br>
- Error가 추가되었다.

> 각각의 마이크로서비스가 현재 가지고있는 메모리 상태, 호출된 정확한 횟수, ... 파악하기 위해서는 추가적으로 모니터링 기능을 넣으면 된다.


