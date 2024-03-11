# Configuration Service
yml 파일 내용이 변경되면 애플리케이션 자체를 다시 빌드, 배포해야하는 단점을 설정 파일을 외부 시스템에서 관리하도록 변경하여 해결

## Spring Cloud Config
- 분산 시스템에서 서버 클라이언트 구성에 필요한 설정 정보(application.yml)를 외부 시스템에서 관리
- 하나의 중앙화 된 저장소에서 구성요소 관리 가능
- 각 서비스를 다시 빌드하지 않고, 바로 적용 가능
- 애플리케이션 배포 파이프라인을 통해 DEV(개발)-UAT(테스트)-PROD(운영) 환경에 맞는 구성 정보 사용
- 각 환경에 따라서 설정 정보가 다를 수 있다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ec4ddbd3-6b41-4e80-b9a6-a01af5d34a5d" width="70%"/><br>

- 구성 정보를 파일관리시스템에 저장한 뒤 Cloud Config Server가 가져와서 Microserive 에 데이터(설정 정보)를 전달해주는 과정을 거친다.
- 동적으로 어플리케이션의 구성 정보를 변경할 수 있다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/54395190-a3e4-4551-ba6d-3c96162c0cad" width="70%"/><br>

<br>

## Local Git Repository
git-local-repo 디렉토리 생성<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f2c1ffc3-e716-434d-9cc1-a63d7a486592" width="70%"/><br>

#### ecommerce.yml
```yaml
token:
  expiration_time: 86400000 # 만료 기간 -> 하루
  secret: secret-key # 토큰 키
gateway:
  ip: 내ip
```
- git add .
- git commit -m "upload an application yaml file"

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a6ae6f0c-2b02-4928-bbf0-492e39625b0c" width="70%"/><br>

깃은 Local repo와 remote repo로 나눠져있는데, add 커맨드를 통해 추적관리를 시작<br>
commit을 통해 Local repo에 등록 push를 진행하면 Remote repo로 등록하여 서버와 로컬을 동기화한다.<br>
commit만 진행하면 Local repo에만 등록하는 것이다.

#### 우선순위
설정파일 Repo에 존재하는 yml파일의 우선순위를 지정할 수 있다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e5a252e4-69d6-46cc-99b2-015d26803dad" width="70%"/><br>

해당 마이크로서비스들은 어떤 설정 파일을 사용할 것인지에 대해서 명시한다.-> user-service.yml / profile: prod -> user-service-prod.yml<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7359b054-4566-4b42-bed2-8a6167058363" width="70%"/><br>

<br>

## Spring Cloud Config - 프로젝트 생성
- 라이브러리
  - Config Server

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c1ae6c3b-97f5-4f65-9064-885a10bb26ec" width="70%"/><br>


```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServiceApplication.class, args);
    }
}
```


#### application.yml
```yaml
server:
  port: 8888

spring:
  application:
    name: config-service
  cloud:
    config:
      server:
        git:
          uri: file:///Users/choihyewon/Desktop/Work/git-local-repo

```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ad73ea9d-4b80-4bc9-a56d-0e473c4cbfc3" width="70%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/caf77b85-6c31-4f48-b779-2953c9a14cd9" width="70%"/><br>


<br>

## User Microservice에서 Spring Cloud Config 연동
- 라이브러리 추가
  - spring-cloud-starter-config
  - spring-cloud-starter-bootstrap
- bootstrap.yml 추가
  - `application.yml`보다 우선순위가 높은 파일
  - 읽어오고자 하는 설정 정보의 위치 저장
  - Spring Cloud Config 에 대한 정보를 먼저 등록해 주는 파일

#### bootstrap.yml
```yaml
spring:
  cloud:
    config:
      uri: http://127.0.0.1:8888
      name: ecommerce
```

#### userController
```java
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final Environment env;
    private final UserService userService;

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in User Service"
                        + ", port(local.server.port)=" + env.getProperty("local.server.port")
                        + ", port(server.port)=" + env.getProperty("server.port")
                        + ", token secret=" + env.getProperty("token.secret")
                        + ", token expiration time=" + env.getProperty("token.expiration_time"));
    }
...
}

```
**Discovery-service 실행->Config-service 실행->Users-service 실행**<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9d4fa4f1-1298-4a7b-b286-60cc10667e3b" width="100%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/172ea37b-24c6-40ce-b864-33d6fbb3ad61" width="70%"/><br>
- 외부에 있는 구성정보 파일(`ecommerce.yml`)을 user microservice에서 가져와 호출할 수 있다.
- `ecommerce.yml` 정보 변경 시 
  - git add `ecommerce.yml`
  - git commit -m "changed some values"

### Changed configuration values
Config Server는 언제든지 변경될 수 있는 상태

### 정보 변경 시 다시 가져오는 3가지 방법
1. 서버 재기동 -> 의미 없음
2. Actuator refresh 옵션 사용
3. Spring cloud bus 사용 ** -> 다음 과정

### Spring Boot Actuator
- user service 를 재부팅하지 않고도 정보 반영
- Application 상태, 모니터링
- Metric 수집을 위한 Http End Point 제공
- 라이브러리 추가
  - spring-boot-starter-actuator

#### WebSecurity
WebSecurity에서 /actuator/**에 대한 요청 인증 허가

```java
@Configuration // 다른 빈보다 등록 우선순위가 높아진다.
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private final Environment env; // 설정 정보의 JWT 정보를 가져오기 위한 빈
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    static final String IP = "127.0.0.1";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests().antMatchers("/actuator/**").permitAll();
        http.authorizeRequests().antMatchers("/**")
                .hasIpAddress(IP)
                .and()
                .addFilter(getAuthenticationFilter());

        http.headers().frameOptions().disable(); // h2-console 에 접근하기 위한 disable
    }
...
}
```
#### application.yml
```yaml
...
management:
  endpoint:
    web:
      exposure:
        include: refresh,health,beans
```

**설정 정보 변경 -> post-127.0.0.1/{port}/actuator/refresh**

#### ecommerce.yml
```yaml
token:
  expiration_time: 864 # 만료 기간 -> 하루
  secret: secret-key2 # 토큰 키
gateway:
  ip: 127.0.0.1
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/75cd3578-cf9b-41a9-a5f3-c705cb88ff77" width="70%"/><br>
- body 에 변경된 부분 표시 <br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/71f1fbdc-68ef-4680-8cbb-e34fb0624399" width="70%"/><br>

<br>

## Spring Cloud Gateway에서 Spring Cloud Config 연동
- 라이브러리 추가
  - spring-boot-start-config
  - spring-boot-start-bootstrap
  - spring-boot-start-actuator

#### bootstrap.yml
```yaml
spring:
  cloud:
    config:
      uri: http://127.0.0.1:8888
      name: ecommerce
```

#### application.yml
```yaml
- id: user-service
  uri: lb://USER-SERVICE
  predicates:
    - Path=/user-service/actuator/**
    - Method=GET, POST
  filters:
    - RemoveRequestHeader=Cookie
    - RewritePath=/user-service/(?<segment>.*), /$\{segment}

...
management:
  endpoints:
    web:
      exposure:
        include: refresh, health, beans, httptrace
```
- `httptrace`: 클라이언트 요청이 들어와서 스프링부트에 구성되어있는 각각의 Microservice들의 호출, 처리되는 상태같은 tracing을 확인할 수 있는 기능

```java
@Bean
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }
```
- 위와 같이 HttpTraceRepository 를 빈으로 등록하게 되면 클라이언트가 요청했던 트레이스 정보가 메모리에 담겨서 필요할 때 엔드포인트로 확인할 수 있다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e5c41383-858c-4a35-afe2-2e391f858757" width="100%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4b60323e-e8f7-44a8-9316-012047d044c4" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a7b0402e-fa49-4f74-b330-7014806a613c" width="100%"/><br>
- 로그인 후 header 에 token 값 복사<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ed47da63-2ce4-4fa0-8554-84363ff75cf6" width="100%"/><br>

<br>

## Profiles을 사용한 Configuration 적용
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3fea48e4-c700-4307-966a-de59d7c51837" width="100%"/><br>

설정 파일 추가<br>
#### ecommerce-dev.yml
```yaml
token:
  expiration_time: 86400000 # 만료 기간 -> 하루
  secret: secret-key-dev # 토큰 키
gateway:
  ip: 127.0.0.1

```
#### ecommerce-prod.yml
```yaml
token:
  expiration_time: 86400000 # 만료 기간 -> 하루
  secret: secret-key-prod # 토큰 키
gateway:
  ip: 127.0.0.1

```
`bootstrap.yml`을 통해서 Config Server 로부터 가져올 profile 설정

<br>

#### user-service - bootstrap.yml
```yaml
spring:
  cloud:
    config:
      uri: http://127.0.0.1:8888
      name: ecommerce

  profiles:
    active: dev
```


#### apigateway-service - bootstrap.yml
```yaml
spring:
  cloud:
    config:
      uri: http://127.0.0.1:8888
      name: ecommerce

  profiles:
    active: prod
```