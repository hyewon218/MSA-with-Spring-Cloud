package org.example.usersservice.service;

import java.util.List;
import org.example.usersservice.dto.UsersRequestDto;
import org.example.usersservice.dto.UsersResponseDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UsersService extends UserDetailsService {

    UsersResponseDto createUser(UsersRequestDto requestDto);

    List<UsersResponseDto> getAllUsers();

    UsersResponseDto getUserByUserId(String userId);
}