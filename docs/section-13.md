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





