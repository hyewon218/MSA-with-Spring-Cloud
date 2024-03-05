# Users Microservice - 2
- Features
  - 신규 회원 등록 X
  - 회원 로그인
  - 상세 정보 확인 X
  - 회원 정보 수정/삭제
  - 상품 주문 - X
  - 주문 내역 확인 - X

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a7d80e1b-b4e7-48b0-9e35-2731957b63f5" width="70%"/><br>

로그인에 성공하면 클라이언트는 서버로 부터 응답 헤더를 통해 token, userId를 반환 받는다.

## Users Microservice - AuthenticationFilter 추가
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

#### AuthenticationFilter
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
- `RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);`: 전달되어진 InputStream 에 어떠한 값이 들어있을 때 원하는 자바 클래스 타입으로 변형,
- InputStream 으로 받은 이유는 전달받고하는 로그인의 값은 Post 형태로 전달되어 RequestParam 으로 받을 수 없기 때문에 InputStream 으로 처리한다.
- `return getAuthenticationManager().authenticate(...));`
  - 사용자가 입력한 이메일과 아이디 값을 스프링 시큐리티에서 사용할 수 있는 값으로 UsernamePasswordAuthenticationToken으로 변형
  - UsernamePasswordAuthenticationToken 값을 AuthenticationManager 에 전달하여 인증 요청
  - 토큰을 전달받은 AuthenticationManager 는 아이디와 패스워드를 통해 비교작업 수행

#### WebSecurity
- 기존의 WebSecurity 는 모든 /users/로 들어오는 요청을 permit
```java
@Configuration // 다른 빈보다 등록 우선순위가 높아진다.
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
//        http.authorizeRequests().antMatchers("/user-service/users/**").permitAll();
        http.authorizeRequests().antMatchers("/**")
                .hasIpAddress("192.168.80.1") // 통과시키고자하는 IP 주소
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
- `http.authorizeRequests().antMatchers("/**")`: 모든 요청을 검증
  - `.hasIpAddress("192.xxx.xx.1")`: 통과시킬 아이피 주소
  - `.addFilter(getAuthenticationFilter());` : 앞에서 만든 AuthenticationFilter 추가
- `authenticationFilter.setAuthenticationManager(authenticationManager());` 인증처리를 하기위한 매니저로 스프링 시큐리티에서 매니저를 가져와서 set

정리하면 모든 요청을 통과시키지 않을 것이며 사용자의 아이피는 제한적이며 해당 필터를 통과한 데이터에만 권한을 부여하고 작업을 진행시킨다.

<br>

## Users MicroService - loadUserByUsername() 구현
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
- protected void configure(HttpSecurity http) throws Exception {} : 권한에 관련된 부분
- protected void configure(AuthenticationManagerBuilder auth) throws Exception {}: 인증에 관련된 부분
- 인증에 있어서 AuthenticationManagerBuilder 가 가진 userDetailService 를 사용
- userDetailService 는 사용자가 전달한 내용을 가지고 로그인 처리를 해준다.
- 먼저 사용자 데이터를 가져와서 입력으로 받은 비밀번호를 암호화하여 데이터베이스의 암호화비밀번호와 비교한다.

#### UserService
```java
public interface UserService extends UserDetailsService {

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
- `return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(), ...);`: 사용자 이메일을 통해 검색이 완료되면<br>
pwd 비교하고 pwd 비교가 완료되면 검색된 사용자값 반환, new ArrayList는 권한 목록

<br>

## Users Microservice - Routes 정보 변경
#### apigateway-service
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
  - RemoveRequestHeader=Cookie: POST로 전달되어오는 값은 매번 새롭게 새로운 데이터처럼 인식하기 위해서 RequestHeader값을 초기화한다
- filters:
  - RewritePath=/user-service/(?,*), /${segment}: 요청으로 들어온 패턴 중에서 /user-service는 빼고 rewrite

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

