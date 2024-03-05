package org.example.usersservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.example.usersservice.dto.LoginRequestDto;
import org.example.usersservice.dto.UsersResponseDto;
import org.example.usersservice.service.UsersService;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UsersService usersService;
    private Environment environment;

    /**
     * - attemptAuthentication 으로 인증 시도
     *    - 사용자가 입력한 id,password 같은 인증 정보를 기반으로
     *    - bean 으로 등록된 AuthenticationManager 을 통해 자동 으로 인증 시도(using UserDetailsService)
     * - successfulAuthentication 으로 인증 후 jwt 발급 (using Authentication)
     **/
    public AuthenticationFilter(AuthenticationManager authenticationManager, UsersService usersService, Environment environment) {
        super(authenticationManager); // super.setAuthenticationManager(authenticationManager);
        this.usersService = usersService;
        this.environment = environment;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
        throws AuthenticationException {
        try {

            LoginRequestDto creds = new ObjectMapper().readValue(req.getInputStream(), LoginRequestDto.class);

            return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword(), new ArrayList<>()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* 인증 성공 `후` 처리 해야 할 부분 구현*/
    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
        Authentication auth) throws IOException, ServletException {

        log.info("successfulAuthentication called");

        String userName = ((User) auth.getPrincipal()).getUsername();
        UsersResponseDto userDetails = usersService.getUserDetailsByEmail(userName);

        byte[] secretKeyBytes = Base64.getEncoder().encode(environment.getProperty("token.secret").getBytes());

        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

        Instant now = Instant.now();

        String token = Jwts.builder()
            .subject(userDetails.getUserId())
            .expiration(Date.from(now.plusMillis(Long.parseLong(environment.getProperty("token.expiration_time")))))
            .issuedAt(Date.from(now))
            .signWith(secretKey)
            .compact();

        res.addHeader("token", token);
        res.addHeader("userId", userDetails.getUserId());
    }
}