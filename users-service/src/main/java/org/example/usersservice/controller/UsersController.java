package org.example.usersservice.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.usersservice.dto.UsersRequestDto;
import org.example.usersservice.dto.UsersResponseDto;
import org.example.usersservice.service.UsersService;
import org.example.usersservice.vo.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users-service")
@RequiredArgsConstructor
public class UsersController {

    private final Environment env;
    private final UsersService usersService;

    @Autowired
    private Greeting greeting;

/*    @Autowired
    public UsersController(Environment env) { // 생성자 주입
        this.env = env;
    }*/

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in User Service on PORT %s",
            env.getProperty("local.server.port"));
    }

    @GetMapping("/welcome")
    public String welcome() {
        //return env.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<UsersResponseDto> createUser(@RequestBody UsersRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usersService.createUser(requestDto));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UsersResponseDto>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(usersService.getAllUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UsersResponseDto> getUser(@PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(usersService.getUserByUserId(userId));
    }
}