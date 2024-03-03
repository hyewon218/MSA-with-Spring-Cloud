# Catalogs and Orders Microservice

## Users Microservice 기능 추가
- 상세 정보 확인, 주문 내역 확인

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ee9f8be7-a653-4992-9cab-41c13dafa37f" width="70%"/><br>

### UserController
```java
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final Environment env;
    private final UserService userService;


    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in User Service on PORT %s",
                env.getProperty("local.server.port"));
    }

...
}
```
- `env.getProperty("local.server.port")`: 랜덤 포트로 할당된 설정 값을 가져온다.<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3cddd4f9-37d7-49a0-894e-601f8f63932a" width="40%"/><br>

<br>

## Users Microservice 와 Spring Cloud Gateway 연동

### apigateway-service
#### application.yml

```yaml
server:
...
      routes:
        - id: user-service
          uri: lb://USERS-SERVICE   # data 을 forwarding 할 eureka server
          predicates:
            - Path=/user-service/** # 실제 클라이언트의 요쳥 형식
-    ## 결국 /users-service/** 로 요청이 들어오면 USERS-SERVICE eureka 로 forwarding
```
- User Service route 추가

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7e843396-d253-4115-bb58-2a278511b4d3" width="80%"/><br>
- 아래쪽은 gateway 를 거치지 않고 user service 를 다이렉트로 접속했을 때 - user service 직접 호출<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3cddd4f9-37d7-49a0-894e-601f8f63932a" width="40%"/><br>
- 현재 User Service 의 URI 와 API Gateway 의 URI 가 다르다.<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4741f370-658e-41f7-a27f-05172c0b7994" width="60%"/><br>
- UserController 의 Root Request Mapping 정보를 변경
  ```java
  @RestController
  @RequestMapping("/user-service")
  @RequiredArgsConstructor
  public class UserController {

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in User Service on PORT %s",
                env.getProperty("local.server.port"));
    }
  ```
  - user service 직접 호출 <br>
    <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/da337772-e48e-4d6a-b64a-9d8ee65f1113" width="40%"/><br>
  - gateway 로 호출 <br>
    <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/dd145d33-2867-412a-8e41-521cc08469a8" width="40%"/><br>

<br>

## User Microservice - 사용자 조회
#### ResponseUser
```java
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUser {

    private String email;
    private String name;
    private String userId;
    
    private List<ResponseOrder> orders;

}
```
- `@JsonInclude(JsonInclude.Include.NON_NULL)`: 불필요한 값인 null 데이터는 버리고 전달

#### ResponseOrder
```java
@Data
public class ResponseOrder {

    private String productId;
    private Integer qty;
    private Integer unitPrice; // 단가
    private Integer totalPrice;
    private Date createdAt;
    
    private String orderId;

}
```
#### UserDto
```java
@Data
public class UserDto {
    private String email;
    private String name;
    private String pwd;
    private String userId;
    private Date createdAt;

    private String encryptedPwd;

    private List<ResponseOrder> orders = new ArrayList<>();
}
```
#### UserService
```java
public interface UserService {

    UserDto create(UserDto userDto);

    UserDto getUserByUserId(String userId);

    List<UserEntity> getUserByAll();

}
```
#### UserServiceImpl
```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    ...

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("USer not found"));

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
        List<ResponseOrder> orderList = new ArrayList<>();
        userDto.setOrders(orderList);

        return userDto;
    }

    @Override
    public List<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }
}
```
#### UserRepository
```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserId(String userId);

}
```
#### UserController
```java
@RestController
@RequestMapping("/user-service")
@RequiredArgsConstructor
public class UserController {

    ...

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {

        List<UserEntity> userList = userService.getUserByAll();
        List<ResponseUser> result = new ArrayList<>();
        ModelMapper mapper = new ModelMapper();
        userList.forEach(v -> result.add(mapper.map(v, ResponseUser.class)));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId) {
        UserDto userDto = userService.getUserByUserId(userId);

        ResponseUser result = new ModelMapper().map(userDto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/044467df-a08e-4365-a637-d32d9b637400" width="70%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/723413ee-fab7-4452-9c55-13a3a44c4ec6" width="70%"/><br>

<br>

## Catalogs Microservice

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a7d80e1b-b4e7-48b0-9e35-2731957b63f5" width="70%"/><br>

라이브러리 추가
- Spring Web
- Devtools
- Lombok
- Jpa
- Eureka Discovery Client
- Model Mapper
- h2

### catalog-service
#### application.yml
```yaml
server:
  port: 0

spring:
  application:
    name: catalog-service
  h2:
    console:
      enabled: true
      settings:
        web-allow-others: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    generate-ddl: true
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb

eureka:
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka
logging:
  level:
    org.example.catalogsservice:: DEBUG
```
- `jpa.hibernate.ddl-auto`: create-drop: 애플리케이션이 기동되면서 초기에 만들어야하는 데이터를 sql파일에 등록해두고 해당 데이터파일을 자동으로 insert해주는 작업을 해줄 수 있다.
- `jpa.show-sql`: sql문 화면에 출력
- `jpa.ddl-auto`: ddl문장 화면에 출력

#### CatalogEntity
```java
@Data
@Entity
@Table(name = "catalog")
public class CatalogEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String productId;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private Integer stock;
    @Column(nullable = false)
    private Integer unitPrice;

    @Column(nullable = false, updatable = false, insertable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private Date createdAt;

}
```
- @ColumnDefault(value = "CURRENT_TIMESTAMP"): H2 DB 에서 현재시간을 가져오기 위한 함수이름

