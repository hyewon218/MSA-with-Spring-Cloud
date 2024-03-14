# Users Microservice - 2
- Features
  - 신규 회원 등록 X
  - 회원 로그인
  - 상세 정보 확인 X
  - 회원 정보 수정/삭제
  - 상품 주문 - X
  - 주문 내역 확인 - X

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a7d80e1b-b4e7-48b0-9e35-2731957b63f5" width="70%"/><br>

### Users Microservice 기능 추가 - login
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2bd0c032-04e4-4023-83d7-f450b360bf26" width="70%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3a077b82-2c3e-4fab-b587-3534db16da20" width="70%"/><br>
- 8000 : gateway port 번호
- 로그인에 성공하면 클라이언트는 서버로 부터 응답 헤더를 통해 token, userId를 반환 받는다.

### Users Microservice - AuthenticationFilter 추가
#### RequestLogin
```java
@Data
public class RequestLogin {
    
    @NotNull(message = "Email cannot be null")
    @Size(min = 2, message = "Email not be less than two characters")
    @Email
    private String email;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "Password must be equals or grater than 8 characters")
    private String password;
}
```

#### AuthenticationFilter : 인증
Spring Security를 이용한 로그인 요청 발생 시 작업을 처리해 주는 Custom Filter 클래스<br>
UsernamePasswordAuthenticationFilter 상속

```java
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication
            (HttpServletRequest request,
             HttpServletResponse response) throws AuthenticationException {

        try {
            RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getEmail(),
                            creds.getPassword(),
                            new ArrayList<>())
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
       
    }
}
```
- `RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);`: 전달된 InputStream 에 어떠한 값이 들어있을 때 원하는 자바 클래스 타입으로 변형,
- InputStream 으로 받은 이유는 전달받고자 하는 로그인의 값은 Post 형태로 전달되어 RequestParam 으로 받을 수 없기 때문에 InputStream 으로 처리한다.
- `return getAuthenticationManager().authenticate(...));`
  - 사용자가 입력한 이메일과 아이디 값을 스프링 시큐리티에서 사용할 수 있는 값으로 UsernamePasswordAuthenticationToken으로 변형
  - UsernamePasswordAuthenticationToken 값을 AuthenticationManager 에 전달하여 인증 요청
  - 토큰을 전달받은 AuthenticationManager 는 아이디와 패스워드를 통해 비교작업 수행

#### WebSecurity
사용자 요청에 대해 AuthenticationFilter를 거치도록 수정

```java
@Configuration // 다른 빈보다 등록 우선순위가 높아진다.
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
//      http.authorizeRequests().antMatchers("/user-service/users/**").permitAll();
        http.authorizeRequests().antMatchers("/**")
                .hasIpAddress("192.168.80.1") // 통과시키고자 하는 IP 주소
                .and()
                .addFilter(getAuthenticationFilter());

        http.headers().frameOptions().disable(); // h2-console 에 접근하기 위한 disable
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {

        AuthenticationFilter authenticationFilter = new AuthenticationFilter();
        authenticationFilter.setAuthenticationManager(authenticationManager());

        return authenticationFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```
- 기존의 WebSecurity 는 모든 /users/로 들어오는 요청을 permit
- `http.authorizeRequests().antMatchers("/**")`: 모든 요청을 검증
  - `.hasIpAddress("192.xxx.xx.1")`: 통과시킬 아이피 주소
  - `.addFilter(getAuthenticationFilter());` : 앞에서 만든 AuthenticationFilter 추가
- `authenticationFilter.setAuthenticationManager(authenticationManager());` 인증처리를 하기위한 매니저로 스프링 시큐리티에서 매니저를 가져와서 set

정리하면 모든 요청을 통과시키지 않을 것이며 사용자의 아이피는 제한적이며 해당 필터를 통과한 데이터에만 권한을 부여하고 작업을 진행시킨다.

<br>

### Users MicroService - loadUserByUsername() 구현
#### WebSecurity
인증처리를 위한 configure 추가
```java
@Configuration // 다른 빈보다 등록 우선순위가 높아진다.
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private final Environment env;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    ...

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
        super.configure(auth);
    }
}
```
- protected void configure(`HttpSecurity http`) throws Exception {} : 권한에 관련된 부분
- protected void configure(`AuthenticationManagerBuilder auth`) throws Exception {}: 인증에 관련된 부분
- 인증에 있어서 `AuthenticationManagerBuilder` 가 가진 `userDetailService` 를 사용
- userDetailService 는 사용자가 전달한 내용을 가지고 **로그인 처리**를 해준다.
- 먼저 **사용자 데이터**를 가져와서 입력으로 받은 비밀번호를 암호화하여 **데이터베이스의 암호화비밀번호**와 비교한다.

#### UserService
```java
public interface UserService extends UserDetailsService {

    UserDto create(UserDto userDto);
    UserDto getUserByUserId(String userId);
    List<UserEntity> getUserByAll();

}
```
- 인증을 위해 UserDetailsService 를 상속받아 구현

#### UserServiceImpl
```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
...
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity userEntity = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return new User(userEntity.getEmail(),
            userEntity.getEncryptedPwd(),
            false, false, false, false,
            new ArrayList<>());
  }
}
```
- loadUserByUsername() 추상메소드 구현
- `return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(), ...);`: 사용자 이메일을 통해 검색이 완료되면<br>
pwd 비교하고 pwd 비교가 완료되면 검색된 사용자값 반환, new ArrayList는 권한 목록

<br>

## Users Microservice - Routes 정보 변경
#### apigateway-service
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d767773b-44b2-48b6-bf89-0d81b0b87fe3" width="80%"/><br>
```yaml
...
      routes:
#        - id: user-service
#          uri: lb://USER-SERVICE
#          predicates:
#            - Path=/user-service/**
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/user-service/login
            - Method=POST
          filters:
            - RemoveRequestHeader=Cookie
            - RewritePath=/user-service/(?<segment>,*), /$\{segment}
        - id: user-service
            uri: lb://USER-SERVICE
            predicates:
              - Path=/user-service/users
              - Method=POST
            filters:
              - RemoveRequestHeader=Cookie
              - RewritePath=/user-service/(?<segment>,*), /$\{segment}
        - id: user-service
            uri: lb://USER-SERVICE
            predicates:
              - Path=/user-service/**
              - Method=GET
            filters:
              - RemoveRequestHeader=Cookie
              - RewritePath=/user-service/(?<segment>,*), /$\{segment}
```
- filters:
  - `RemoveRequestHeader=Cookie`: POST로 전달되어오는 값은 매번 새롭게 새로운 데이터처럼 인식하기 위해서 RequestHeader값을 초기화한다
  - `RewritePath`=/user-service/(?,*), /${segment}: 요청으로 들어온 패턴 중에서 /user-service는 빼고 rewrite
    - `127.0.0.1:8000/users-service/users` 가 user microservice 에 실제로 전달되는 값은 `:60000(user포트번호)/users`

#### UserController
```java
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController { ... }
```
```java
@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    ...
  

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        log.debug(((User)authResult.getPrincipal()).getUsername());

    }
}
```
- UserDetailService 의 loadUserByUsername 에 의해 반환된 User 객체를 successfulAuthentication 에서 확인할 수 있다.
과정 attempAuthentication -> loadUserByUsername -> successfulAuthentication
- attempAuthentication: 사용자가 요청 메시지에 담은 데이터를 RequestLogin으로 변경
- loadUserByUsername: username을 통해 DB에서 엔티티 조회하여 엔티티를 통해 User 객체 생성
- successfulAuthentication: 로그인 로직 성공 후 작업

<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b9adf709-c52b-49ba-bfac-d561d3b4feee" width="80%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/13068bdc-572b-4191-b2d1-a373ce4291e9" width="100%"/><br>

<br>

## JWT(Json Web Token) 생성
- 인증 헤더 내에서 사용되는 토큰 포맷
- 두 개의 시스템끼리 안전한 방법으로 통신 가능

<br>

- jsonwebtoken 라이브러리 추가

```yaml
...
token:
  expiration_time: 86400000 # 만료 기간 -> 하루
  secret: secret-key # 토큰 키
```
```java
@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UserService userService;
    private Environment env;


    ...

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        String userName = ((User) authResult.getPrincipal()).getUsername();
        UserDto userDto = userService.getUserDetailsByEmail(userName);

        String token = Jwts.builder()
                .setSubject(userDto.getUserId())
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(env.getProperty("token.expiration_time"))))
                .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
                .compact();

        response.addHeader("token", token);
        response.addHeader("userId", userDto.getUserId());

    }
}
```
- Long.parseLong : yml 파일에 있는 정보 모두 string 값으로 가져오기 때문에 숫자 형태로 바꿔주기
- signWith : 암호화

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/069f7c1a-2daf-4657-bafa-9080c7120251" width="100%"/><br>

<br>

### JWT 처리 과정
**전통적인 인증 시스템**
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b1730e8d-7886-4fd8-af2c-2d6fb03a93c0" width="100%"/><br>
기존의 웹앱은 클라이언트한테 보여주는 웹앱의 기술(HTML, JSP, ...) 들은 서버단에 구현 기술이었다.<br> 
그렇기 때문에 세션, 쿠키 연동에 문제가 없었지만 예를 들어 모바일 기기는 별도의 실행환경과 개발환경을 가지기 때문에<br> 
서버가 자바로 개발되었다면 자바에서 발급한 세션과 쿠키를 연동하기 힘들다.

- 문제점
  - 세션과 쿠키는 모바일 애플리케이션과 같이 이기종에서 유효하게 사용할 수 없음(**공유 불가**)
  - 렌더링된 HTML 페이지가 반환되지만, 모바일 애플리케이션에서는 **JSON**(or XML)과 같은 포맷 필요

**Token 기반 인증 시스템**
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/fa3f391c-79cf-4d35-ad7a-0f06893f9b40" width="100%"/><br>
- 서버는 세션이 아닌 토큰을 발급한다.
- 사용자는 이 토큰을 가지고 추가적인 서비스 요청을 한다.(인증이 끝났으니 권한이 있다는 것)
- 서버는 토큰 일치 여부를 따진 후 추가적인 정보 처리

<br>

### JWT 장점
- 클라이언트 독립적인 서비스 (stateless)
- **CDN(Contents Delivery Network)**: 중간에 캐시 서버를 두는 기술
- No Cookie-Session(No CSRF, 사이트간 요청 위조 가능성 낮아짐)
- 지속적인 토큰 저장

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4db90c87-e4a5-4de7-b91d-87ec08888e31" width="80%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b244d3cc-d45b-4eb9-b496-a1524d89bcc3" width="80%"/><br>

- **JWT를 DB에 저장**함으로써 서버 간에 세션 공유를 하지 않더라도 **다른 Microservice에서도 사용 가능**

<br>

### Apl Gateway service에 Spring Security와 JWT Token 사용 추가

```yaml
- id: user-service
  uri: lb://USER-SERVICE
  predicates:
    - Path=/user-service/** # health_check, welcome, ...
    - Method=GET
  filters:
    - RemoveRequestHeader=Cookie
    - RewritePath=/user-service/(?<segment>.*), /$\{segment}
    - AuthorizationHeaderFilter # jwt 인증 관련 필터
```
- 로그인과 회원가입은 jwt를 검증하는 과정이 필요없기 때문에 나머지 작업에서만 jwt 인증

#### apigateway-service
- jwt 라이브러리 추가 (정상적인 jwt 인지 decode 진행)

#### AuthorizationHeaderFilter
- API 호출 시 **헤더에 로그인 시 받은 토큰을 전달**해주는 작업 진행 -> 토큰이 존재하는지, 적절한 인증인지, 토큰이 제대로 발급되었는지, ...
```java
@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    public Environment env;

    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    // API 호출 시 헤더에 로그인 시 받은 토큰을 전달해주는 작업 진행
    @Override
    public GatewayFilter apply(Config config) {
        return (((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 헤더에 존재하는 검증
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "no authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace("Bearer", "");

            // jwt 검증
            if (!isJwtValid(jwt)) {
                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);

        }));
    }

    private boolean isJwtValid(String jwt) {
        boolean returnValue = true;

        // JWT subject 를 추출하여 검증
        String subject = null;

        try {
            subject = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                    .parseClaimsJwt(jwt).getBody()
                    .getSubject();
        } catch (Exception e) { // 파싱 중 오류 처리
            returnValue = false;
        }

        if (subject == null || subject.isEmpty()) {
            returnValue = false;
        }

//        if (!subject.equals()) {
//            returnValue = false;
//        }


        return returnValue;

    }

    // Mono(단일), Flux(다중) -> Spring WebFlux
    // Spring Cloud Gateway Service 는 기존의 Spring MVC로 구성하지 않는다.
    // HttpServletRequest, HttpServletResponse 를 사용할 수 있는 Spring MVC이 아닌 Spring Web Flux 를 사용함으로써 비동기 방식으로 데이터를 처리하게된다.
    // 비동기 방식에서 데이터를 처리하는 2가지 방법 중 하나인 Mono(단일값) -> Mono라는 단일값에 데이터를 넣어서 반환할 수 있다.
    // 단일값이 아닌 다중값 데이터에 대해서는 Flux 라는 형태로 반환

    // 에러 메시지 반환
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(err);
        return response.setComplete();
    }

    public static class Config {

    }
}

```
- JWT 를 decode 하여 결과물이 userId 와 같은지 확인

Header 존재 X<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f2f4d17b-508e-4138-a577-f5eed601c06a" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9c6b3987-973d-4376-9ca8-45ed6cb7dcf0" width="100%"/><br>
Header 존재 O<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/069f7c1a-2daf-4657-bafa-9080c7120251" width="100%"/><br>

Spring Cloud Gateway Service 는 기존의 Spring MVC로 구성하지 않는다.<br>
따라서 HttpServletRequest, HttpServletResponse 를 사용할 수 있는 Spring MVC이 아닌 `Spring Web Flux` 를 사용함으로써 **비동기 방식**으로 데이터를 처리하게된다.<br>
비동기 방식에서 데이터를 처리하는 2가지 방법 중 하나인 **Mono(단일값)** -> Mono 단일값에 데이터를 넣어서 반환할 수 있다.<br>
단일값이 아닌 데이터에 대해서는 **Flux**라는 형태로 반환

