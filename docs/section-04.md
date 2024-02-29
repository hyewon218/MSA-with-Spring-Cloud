# Users Microservice - 1

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/140269cb-deb8-4572-b51e-e23d6f41f29b" width="60%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b523d40d-b9cf-4720-9569-e4e10ed5a33c" width="30%"/><br>
#### Features
- 신규 회원 등록
- 회원 로그인
- 상세 정보 확인
- 회원 정보 수정/삭제
- 상품 주문
- 주문 내역 확인

### APIs
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/85720771-6ab1-4808-9e5d-24a4ae57ffc3" width="70%"/><br>

## Users Microservice - 프로젝트 생성
라이브러리 추가
- Lombok
- H2
- Spring Boot DevTools: 웹 앱을 수정한 뒤 종료했다가 다시 키지않아도 reload해주는 기능 포함된 라이브러리
- Spring Web
- Eureka Discovery Client
- JPA
- Model Mapper
- Spring Security

### Eureka 서버에 등록
```java
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
```

### Configuration 정보 추가
```yaml
server:
  port: 0

spring:
  application:
    name: user-service
    
eureka:
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka

greeting:
  message: Welcome to the Simple E-Commerce.
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ee3b665e-3f57-4736-9dfb-32750de276b6" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/400d72ec-1fe5-4cba-b709-644cb6902852" width="40%"/><br>

#### 해당 설정 정보를 사용하는 2가지 방법
- Environment 사용
- @Value 사용

<br>

#### Environment 사용
### UserController
```java
@RestController
@RequestMapping("/")
public class UsersController {

    Environment env;

    @Autowired
    public UsersController(Environment env) {
        this.env = env;
    }

    @GetMapping("/health_check")
    public String status() {
        return "It's Working in User Service";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return env.getProperty("greeting.message");
    }
}
```

<br>

#### @Value 사용
### Greeting.java
```java
import org.springframework.beans.factory.annotation.Value;

@Component
@Data
public class Greeting {

    @Value("${greeting.message}")
    private String message;
}
```
### UserController
```java
@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private Greeting greeting;

    @GetMapping("/health_check")
    public String status() {
        return "It's Working in User Service";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return greeting.getMessage();
    }

}
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/447c4160-6570-45e8-a238-81bb53de50c2" width="40%"/><br>

<br>

## Users Microservice - H2 데이터베이스 연동
```yaml
server:
  port: 0

spring:
  application:
    name: user-service
  h2:
    console:
      enabled: true
      settings:
        web-allow-others: true
      path: /h2-console

...
```
#### H2 Database
- 자바로 작성된 오픈소스 RDBMS
- Embedded, Server-Client 가능
- JPA 연동 가

Dependency 추가
```xml
    <!-- https://mvnrepository.com/artifact/com.h2database/h2 -->
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
    </dependency>
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ebb87877-0136-442c-9b5b-b7b34f00c96c" width="60%"/><br>

<br>

## User Microservice - 사용자 추가
### 회원 가입
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2f77c3cc-4ad8-4732-a1c5-74455326410f" width="70%"/><br>

#### RequestUser
- 사용자의 요청으로 들어올 객체
```java
@Data
public class RequestUser {

    @NotNull(message = "Email cannot be null")
    @Size(min = 2, message = "Email not be less than two characters")
    @Email
    private String email;

    @NotNull(message = "Name cannot be null")
    @Size(min = 2, message = "name not be less than two characters")
    private String name;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "Password must be equal or grater than 8 characters")
    private String pwd;

}
```

#### UsersDto
```java
@Data
public class UserDto {
    private String email;
    private String name;
    private String pwd;
    private String userId;
    private Date createdAt;
    
    private String encryptedPwd;
}
```

#### Users
- 데이터베이스로 만들어져야하는 요소
```java
@Data
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false,unique = true)
    private String userId;

    @Column(nullable = false,unique = true)
    private String encryptedPwd;
}
```
#### UsersService
```java
public interface UserService {

    UserDto create(UserDto userDto);
}
```
#### UsersServiceImpl
```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        // 매퍼가 매칭시킬 수 있는 환경 설정 정보 지정
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd("encrypted_password");

        UserDto returnUserDto = mapper.map(userEntity, UserDto.class);
        return returnUserDto;
    }
}
```
#### UsersController
```java
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final Environment env;
    private final UserService userService;

   ...

    @PostMapping("/users")
    public String createUser(@RequestBody RequestUser user) {

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        userService.create(userDto);

        return "Create user method is called";
    }
}
```
Post 에 의해서 정상적으로 데이터가 반영된 경우에는 201("created OK")라는 성공메시지가 더욱 정확한 방법이다.

#### ResponseUser
```java
@Data
public class ResponseUser {

    private String email;
    private String name;
    private String userId;

}
```
#### UsersController
```java
 @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        userService.create(userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }
```

<br>

## User Microservice - Security
- 회원가입 시 입력받은 비밀번호를 암호화해서 데이터베이스에 저장하는 작업을 진행
- Authentication + Authorization

1. step1: 애플리케이션에 **spring security jar**을 Dependency에 추가
2. step2: `WebSecurityConfigurerAdapter`를 상속받는 `Security Configuration` 클래스 생성
3. step3: Security Configuration 클래스에 `@EnableWebSecurity` 추가
4. step4: Authentication -> `configure(AuthenticcationManagerBuilder auth)` 메서드를 재정의
5. step5: Password encode를 위한 `BcryptPasswordEncoder` 빈 정의
6. step6: Authorization -> `configure(HttpSecurity http)` 메서드를 재정의

### WebSecurity
로그인은 구현되지 않았으므로 인증은 통과되었다고 가정

```java
@Configuration // 다른 빈보다 등록 우선순위가 높아진다.
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests().antMatchers("/users/**").permitAll();
        http.headers().frameOptions().disable(); // h2-console 에 접근하기 위한 disable
    }
}
```
- `@Configuration`: 다른 빈보다 등록 우선순위가 높아진다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/babf4380-fb14-476b-b487-e93eacea047f" width="70%"/><br>
위와 같은 오류가 발생하는 이유는 h2 database는 html에 프레임별로 데이터가 나눠져 있기 때문에 무시하는 코드를 추가해야 한다.

<br>

### BCryptPasswordEncoder
- Password를 해싱하기 위해 Bcrypt 알고리즘 사용
- 랜덤 Salt를 부여하여 여러번 Hash를 적용한 암호화 방식 -> 같은 비밀번호가 들어와도 매번 다른 암호화 비밀번호로 저장된다.
- 
#### WebSecurity
```java
@Configuration // 다른 빈보다 등록 우선순위가 높아진다.
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests().antMatchers("/users/**").permitAll();
        http.headers().frameOptions().disable(); // h2-console 에 접근하기 위한 disable
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```
#### UserServiceImpl
```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
}
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ef436c3a-437d-4168-bf1f-1c2dbb291e30" width="70%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/82264ad6-d3c3-4439-9dd6-b541a4f4af35" width="70%"/><br>