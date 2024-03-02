package org.example.usersservice.service;

import java.util.List;
import org.example.usersservice.dto.UsersRequestDto;
import org.example.usersservice.dto.UsersResponseDto;

public interface UsersService {

    UsersResponseDto createUser(UsersRequestDto requestDto);

    List<UsersResponseDto> getAllUsers();

    UsersResponseDto getUserByUserId(String userId);
}