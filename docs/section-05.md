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